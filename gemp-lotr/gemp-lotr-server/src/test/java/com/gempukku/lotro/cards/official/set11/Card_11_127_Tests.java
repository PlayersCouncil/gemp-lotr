package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_127_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goblin", "11_127");
					put("goblin2", "11_127");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("guard", "13_67");        // Guard of the White Tree: [gondor] Man, strength 7, vitality 3; his skirmish ability needs a [gondor] condition, and there is none
					put("elfsong", "1_39");       // Elf-song: an [elven] Fellowship event, here only as a card in hand
					put("defend", "4_115");       // Defend It and Hope: a [gondor] Maneuver event, here only as a card in hand
					put("gimli", "1_13");         // Gimli, Son of Gloin: a Free Peoples card that is no event, here only in hand
					put("drums", "1_168");        // Drums in the Deep: a Shadow event, here only in the Free Peoples hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MockingGoblinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Mocking Goblin
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Each time this minion wins a skirmish, add a burden unless the Free Peoples player discards a Free Peoples event from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("goblin");

		assertEquals("Mocking Goblin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachTimeItWinsTheFreePeoplesPlayerMayDiscardAFreePeoplesEventOrABurdenIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var elfsong = scn.GetFreepsCard("elfsong");
		var defend = scn.GetFreepsCard("defend");
		var gimli = scn.GetFreepsCard("gimli");
		var drums = scn.GetFreepsCard("drums");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(elfsong, defend, gimli, drums);
		assertInHand(elfsong, defend, gimli, drums);

		var goblin = scn.GetShadowCard("goblin");
		var shadowElfsong = scn.GetShadowCard("elfsong");
		scn.MoveCardsToHand(shadowElfsong);
		assertInHand(shadowElfsong);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin);
		// Fierce only so that the same minion fights, and wins, a second skirmish this turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, goblin, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, goblin);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.FreepsGetBurdens());
		assertInHand(elfsong, defend, gimli, drums, shadowElfsong);

		// 12 against Aragorn's 8: the Goblin wins.
		scn.PassSkirmishActions();
		// The loser's wound and this minion's required trigger answer one moment: the wound is placed first.
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard Free Peoples event from hand"));
		scn.FreepsChooseYes();

		// The two Free Peoples events, of two cultures; not the Free Peoples card that is no event, nor the Shadow
		// event in his hand, nor the Shadow player's own copy of Elf-song.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elfsong, defend));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, drums, shadowElfsong));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(defend);

		assertInDiscard(defend);
		assertInHand(elfsong, gimli, drums, shadowElfsong);
		assertEquals(0, scn.FreepsGetBurdens());

		// The fierce skirmish: it wins again and is answered again; declined, the burden is added.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, goblin);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// The loser's wound and this minion's required trigger answer one moment: the wound is placed first.
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard Free Peoples event from hand"));
		scn.FreepsChooseNo();

		assertEquals(1, scn.FreepsGetBurdens());
		assertInHand(elfsong, gimli, drums, shadowElfsong);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesEventInHandTheBurdenIsAddedWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var drums = scn.GetFreepsCard("drums");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gimli, drums);
		assertInHand(gimli, drums);

		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.MoveMinionsToTable(goblin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, goblin);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.FreepsGetBurdens());
		assertInHand(gimli, drums);

		scn.PassSkirmishActions();
		// The loser's wound and this minion's required trigger answer one moment: the wound is placed first.
		scn.FreepsResolveRuleFirst();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard Free Peoples event from hand"));
		assertEquals(1, scn.FreepsGetBurdens());
		assertInHand(gimli, drums);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishAsksNothingWhileTheOtherCopyWinningIsAnsweredOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var elfsong = scn.GetFreepsCard("elfsong");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToHand(elfsong);
		assertInHand(elfsong);

		var goblin = scn.GetShadowCard("goblin");
		var goblin2 = scn.GetShadowCard("goblin2");

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, goblin2);
		// Aragorn 8 + 10 = 18 against 12: the first Goblin loses and, of vitality 3, lives through it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, goblin}, new PhysicalCardImpl[]{guard, goblin2});
		assertEquals(0, scn.FreepsGetBurdens());
		assertInHand(elfsong);

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(goblin));
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard Free Peoples event from hand"));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The second copy beats the Guard (12 against 7): its own trigger, once.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		// The loser's wound and this minion's required trigger answer one moment: the wound is placed first.
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard Free Peoples event from hand"));
		scn.FreepsChooseNo();

		assertEquals(1, scn.FreepsGetBurdens());
		assertInHand(elfsong);
		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard Free Peoples event from hand"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
