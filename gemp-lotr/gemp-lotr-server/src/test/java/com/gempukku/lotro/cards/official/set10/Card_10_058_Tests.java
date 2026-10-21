package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadows", "10_58");

					// Ulaire Otsea, Thrall of The One: an enduring [wraith] Nazgul, vitality 3.  Fierce; a regroup
					// ability, never used.
					put("otsea", "8_81");
					// Candle Corpses: an enduring [wraith] minion of another race (a Wraith), vitality 2.  Its trigger
					// answers its being assigned to a companion, which it never is here.
					put("corpses", "16_2");
					// Orc Slaughterer: an enduring minion who is not [wraith] ([sauron] Orc), vitality 3.
					put("slaughterer", "10_95");
					// Cirith Ungol Warrior: a [wraith] minion who is not enduring, vitality 2.  Its Shadow ability is
					// never used.
					put("warrior", "10_56");

					// Aragorn, Ranger of the North: strength 8, vitality 4.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Sam, Son of Hamfast: a third companion, so that three threats can stand.  Never used.
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DarkSwoopingShadowsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Dark Swooping Shadows
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Heal an enduring [wraith] minion to add a burden or 2 threats.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shadows");

		assertEquals("Dark Swooping Shadows", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void HealsAnEnduringWraithMinionToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var shadows = scn.GetShadowCard("shadows");
		var otsea = scn.GetShadowCard("otsea");
		var corpses = scn.GetShadowCard("corpses");
		var slaughterer = scn.GetShadowCard("slaughterer");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToHand(shadows);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea, corpses, slaughterer, warrior);
		scn.AddWoundsToChar(otsea, 2);
		scn.AddWoundsToChar(corpses, 1);
		scn.AddWoundsToChar(slaughterer, 1);
		scn.AddWoundsToChar(warrior, 1);
		assertInHand(shadows);
		assertEquals(2, scn.GetWoundsOn(otsea));
		assertEquals(1, scn.GetWoundsOn(corpses));
		assertEquals(1, scn.GetWoundsOn(slaughterer));
		assertEquals(1, scn.GetWoundsOn(warrior));

		scn.SkipToAssignments();
		// Only Otsea is assigned; the Corpses are in no skirmish, and are still an enduring [wraith] minion.
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowPlayAvailable(shadows));
		scn.ShadowPlayCard(shadows);

		assertTrue(scn.ShadowHasCardChoiceAvailable(otsea, corpses));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(slaughterer, warrior));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(otsea);

		// One wound healed of two.
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertEquals(1, scn.GetWoundsOn(corpses));
		assertEquals(1, scn.GetWoundsOn(slaughterer));
		assertEquals(1, scn.GetWoundsOn(warrior));

		// Three companions and no threat: both can be done, and the Shadow player is asked.
		assertTrue(scn.ShadowChoiceAvailable("burden"));
		assertTrue(scn.ShadowChoiceAvailable("threats"));
		scn.ShadowChooseOption("burden");

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertInDiscard(shadows);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void HealsAnEnduringWraithMinionToAddTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var shadows = scn.GetShadowCard("shadows");
		var otsea = scn.GetShadowCard("otsea");
		var corpses = scn.GetShadowCard("corpses");
		scn.MoveCardsToHand(shadows);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea, corpses);
		scn.AddWoundsToChar(otsea, 1);
		scn.AddWoundsToChar(corpses, 1);
		assertInHand(shadows);
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertEquals(1, scn.GetWoundsOn(corpses));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertEquals(3, scn.GetThreatLimit());
		scn.ShadowPlayCard(shadows);
		// The one not in the skirmish is chosen this time.
		scn.ShadowChooseCard(corpses);

		assertEquals(0, scn.GetWoundsOn(corpses));
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertTrue(scn.ShadowChoiceAvailable("burden"));
		assertTrue(scn.ShadowChoiceAvailable("threats"));
		scn.ShadowChooseOption("threats");

		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetThreats());
		assertInDiscard(shadows);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WhenTwoThreatsCannotBeAddedABurdenIsAddedWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var shadows = scn.GetShadowCard("shadows");
		var otsea = scn.GetShadowCard("otsea");
		scn.MoveCardsToHand(shadows);

		scn.StartGame();
		scn.AddThreats(1);
		scn.MoveMinionsToTable(otsea);
		scn.AddWoundsToChar(otsea, 1);
		assertInHand(shadows);
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertEquals(1, scn.GetThreats());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Two companions and one threat: only one more threat can be added, so "2 threats" cannot be done in full.
		assertEquals(2, scn.GetThreatLimit());
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		// Otsea is the only enduring [wraith] minion: healed with no choice.
		scn.ShadowPlayCard(shadows);

		assertEquals(0, scn.GetWoundsOn(otsea));
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertInDiscard(shadows);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoWoundedEnduringWraithMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var shadows = scn.GetShadowCard("shadows");
		var otsea = scn.GetShadowCard("otsea");
		var corpses = scn.GetShadowCard("corpses");
		var slaughterer = scn.GetShadowCard("slaughterer");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToHand(shadows);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea, corpses, slaughterer, warrior);
		// The enduring [wraith] minions have no wound to heal; the wounded ones are not enduring [wraith] minions.
		scn.AddWoundsToChar(slaughterer, 1);
		scn.AddWoundsToChar(warrior, 1);
		assertInHand(shadows);
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(corpses));
		assertEquals(1, scn.GetWoundsOn(slaughterer));
		assertEquals(1, scn.GetWoundsOn(warrior));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(shadows));
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var shadows = scn.GetShadowCard("shadows");
		var otsea = scn.GetShadowCard("otsea");
		scn.MoveCardsToHand(shadows);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea);
		scn.AddWoundsToChar(otsea, 1);
		assertInHand(shadows);
		assertEquals(1, scn.GetWoundsOn(otsea));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(shadows));

		// The same board in the skirmish phase: it can be played there.
		scn.ShadowPass();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(shadows));
	}
}
