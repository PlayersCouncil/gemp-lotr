package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_04_223_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("discovered", "4_223");

					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, vitality 2; strength +3 and fierce while 2 burdens can be spotted
					put("explorer", "4_250");   // Southron Explorer: [raider] Man, vitality 3; a regroup ability only
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] minion who is no Man (a Nazgul), vitality 3
					put("chief", "4_20");       // Hill Chief: a [dunland] Man, vitality 2; a Shadow ability only
					put("wreathed", "1_238");   // Wreathed in Shadow: a Shadow condition in the support area (to play, spot a Nazgul: cheated in)

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the [gondor] companion who bears the two items below
					put("saga", "1_114");       // The Saga of Elendil: [gondor] condition borne by a [gondor] companion
					put("sword", "1_112");      // Ranger's Sword: a Free Peoples possession that is no condition, borne by Aragorn
					put("moria", "1_21");       // Lord of Moria: [dwarven] condition in the support area ("Each Dwarf is damage +1")
					put("songs", "1_61");       // Songs of the Blessed Realm: [elven] condition in the support area; answers [elven] tales only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DiscoveredStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Discovered
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert a [raider] Man to discard a Free Peoples condition (or 2 Free Peoples conditions if you spot 3 burdens).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("discovered");

		assertEquals("Discovered", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsARaiderManToDiscardOneFreePeoplesConditionAtTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var discovered = scn.GetShadowCard("discovered");
		var axeman = scn.GetShadowCard("axeman");
		var explorer = scn.GetShadowCard("explorer");
		var khamul = scn.GetShadowCard("khamul");
		var chief = scn.GetShadowCard("chief");
		var wreathed = scn.GetShadowCard("wreathed");

		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var sword = scn.GetFreepsCard("sword");
		var moria = scn.GetFreepsCard("moria");
		var songs = scn.GetFreepsCard("songs");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, saga, sword);
		scn.MoveCardsToSupportArea(moria, songs);
		scn.MoveCardsToHand(discovered);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, explorer, khamul, chief);
		scn.MoveCardsToSupportArea(wreathed);
		// Two burdens and a threat beside them: one short of the 3 burdens, with something that nearly counts.
		scn.AddBurdens(2);
		scn.AddThreats(1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertTrue(scn.ShadowPlayAvailable(discovered));

		scn.ShadowPlayCard(discovered);
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, explorer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, chief));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(explorer);
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(axeman));

		assertTrue(scn.ShadowHasCardChoiceAvailable(saga, moria, songs));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, wreathed));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(saga);

		assertInZone(Zone.DISCARD, saga, discovered);
		assertInZone(Zone.ATTACHED, sword);
		assertInZone(Zone.SUPPORT, moria, songs, wreathed);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AtThreeBurdensTwoFreePeoplesConditionsAreDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var discovered = scn.GetShadowCard("discovered");
		var axeman = scn.GetShadowCard("axeman");
		var wreathed = scn.GetShadowCard("wreathed");

		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var sword = scn.GetFreepsCard("sword");
		var moria = scn.GetFreepsCard("moria");
		var songs = scn.GetFreepsCard("songs");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, saga, sword);
		scn.MoveCardsToSupportArea(moria, songs);
		scn.MoveCardsToHand(discovered);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.MoveCardsToSupportArea(wreathed);
		scn.AddBurdens(3);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(axeman));

		// The one [raider] Man there is exerts unasked.
		scn.ShadowPlayCard(discovered);
		assertEquals(1, scn.GetWoundsOn(axeman));

		assertTrue(scn.ShadowHasCardChoiceAvailable(saga, moria, songs));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, wreathed));
		// The parenthesis is not the player's to decline: two, no fewer.
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(saga, songs);

		assertInZone(Zone.DISCARD, saga, songs, discovered);
		assertInZone(Zone.ATTACHED, sword);
		assertInZone(Zone.SUPPORT, moria, wreathed);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotPlayableWhileTheOnlyRaiderManIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var discovered = scn.GetShadowCard("discovered");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var chief = scn.GetShadowCard("chief");

		var moria = scn.GetFreepsCard("moria");

		scn.MoveCardsToSupportArea(moria);
		scn.MoveCardsToHand(discovered);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, khamul, chief);
		// Vitality 2 with 1 wound: exhausted. Khamul and Hill Chief could exert, but are no [raider] Men.
		scn.AddWoundsToChar(axeman, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertFalse(scn.ShadowPlayAvailable(discovered));

		// With the wound gone he can pay, and the event is offered.
		scn.RemoveWoundsFromChar(axeman, 1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(discovered));
	}
}
