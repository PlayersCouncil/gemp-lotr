package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_158_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("uruk", "12_158");
					put("uruk2", "12_158");
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai minion, vitality 2; Damage +1 is its whole text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability only
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally who exerts to take part in skirmishes
					put("hunt", "1_126");       // Hunt Them Down!: [isengard] Maneuver event, make an Uruk-hai fierce until the regroup phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ViciousUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Vicious Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * At the start of each skirmish involving this minion, you may draw a card for each wound on a character it is skirmishing.
		*/

		var scn = GetScenario();

		var uruk = scn.GetFreepsCard("uruk");

		assertEquals("Vicious Uruk", uruk.getBlueprint().getTitle());
		assertNull(uruk.getBlueprint().getSubtitle());
		assertFalse(uruk.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, uruk.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, uruk.getBlueprint().getCulture());
		assertEquals(CardType.MINION, uruk.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, uruk.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(uruk, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(uruk, Keyword.DAMAGE));
		assertEquals(3, uruk.getBlueprint().getTwilightCost());
		assertEquals(9, uruk.getBlueprint().getStrength());
		assertEquals(2, uruk.getBlueprint().getVitality());
		assertEquals(5, uruk.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachCopyDrawsForTheWoundsOnTheCompanionItSkirmishesAtTheStartOfItsOwnSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruk = scn.GetShadowCard("uruk");
		var uruk2 = scn.GetShadowCard("uruk2");
		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, uruk2, guard);
		// Wounds on the companion it skirmishes count; its own and the other minion's in the same skirmish do not.
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(uruk, 1);
		scn.AddWoundsToChar(guard, 1);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(uruk));
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, uruk}, new PhysicalCardImpl[]{gimli, uruk2});
		scn.ShadowAssignToMinions(aragorn, guard);

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		// At the start of Aragorn's skirmish only the copy in it offers.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(uruk));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(uruk2));
		scn.ShadowChooseOptionalTrigger(uruk);
		// Two wounds on Aragorn: two cards.
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();

		// At the start of Gimli's skirmish only the other copy offers; declined, nothing is drawn.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(uruk2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(uruk));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheCharacterItSkirmishesHasNoWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruk = scn.GetShadowCard("uruk");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		// Wounds elsewhere: on a companion it does not skirmish, and on itself.
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(uruk, 1);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(uruk));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, uruk);
		int hand = scn.GetShadowHandCount();

		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(uruk));
		assertEquals(hand, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DrawsOneForTheWoundOnAnAllyItSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruk = scn.GetShadowCard("uruk");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		// Aragorn is wounded and is in no skirmish: his wounds do not count.
		scn.AddWoundsToChar(aragorn, 2);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.SkipToPhase(Phase.MANEUVER);

		// Thrarin exerts to take part in skirmishes: one wound.
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, uruk);
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		scn.FreepsResolveSkirmish(thrarin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(uruk));
		scn.ShadowChooseOptionalTrigger(uruk);
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsOfferedAgainInItsFierceSkirmishAndDrawsForTheWoundsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruk = scn.GetShadowCard("uruk");
		var hunt = scn.GetShadowCard("hunt");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(hunt);
		assertInHand(hunt);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);

		// Hunt Them Down! makes it fierce, so that it skirmishes twice this turn.
		scn.FreepsPass();
		scn.ShadowPlayCard(hunt);
		assertTrue(scn.HasKeyword(uruk, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, uruk);
		int hand = scn.GetShadowHandCount();

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(uruk));
		scn.ShadowChooseOptionalTrigger(uruk);
		// One wound on Aragorn: one card.
		assertEquals(hand + 1, scn.GetShadowHandCount());

		// 9 against 8: Aragorn loses and takes two wounds (damage +1), and lives with three.
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInPlay(uruk);
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, uruk);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(uruk));
		scn.ShadowChooseOptionalTrigger(uruk);
		// Two wounds on Gimli: two cards more.
		assertEquals(hand + 3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
