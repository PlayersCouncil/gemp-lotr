package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_109_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "17_109");

					// Huorn: [gandalf] Tree companion, strength 6, "strength +1 for each Ent you can spot". He is no Ent.
					put("huorn", "9_25");
					// Ent Avenger: [gandalf] Ent companion, strength 6, "While you can spot 3 Ents, this companion is
					// strength +2". Not unique: two copies.
					put("avenger1", "6_27");
					put("avenger2", "6_27");
					// Urgency: [isengard] support-area condition of the Shadow player's, "Each time the fellowship
					// moves, add (2) for each Ent you can spot": an Ent count that is not Pippin's player's.
					put("urgency", "19_17");

					// Pipeweeds of two cultures and two types, all in the support area.
					put("toby", "1_305");       // Old Toby: [shire] pipeweed possession
					put("ship", "18_27");       // Ship of Smoke: [gandalf] pipeweed condition; its trigger answers a pipeweed played
					put("leaf", "17_106");      // Halfling Leaf: [shire] pipeweed condition; its skirmish ability is not used
					put("toby2", "1_305");      // a pipeweed that is not in play: kept in hand
					put("pipe", "1_285");       // Bilbo's Pipe: a [shire] pipe on a Hobbit, which is no pipeweed

					put("troop", "1_177");      // Goblin Patrol Troop: no text; there so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Pippin, In the Bloom of Health
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 8
		 * Game Text: Add 1 to the number of Ents you can spot.<br><b>Maneuver:</b> Discard 2 pipeweed cards from play to remove a burden.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("In the Bloom of Health", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
	}

	@Test
	public void PlayingPippinAddsOneToEachCountOfEntsHisPlayerMakes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var huorn = scn.GetFreepsCard("huorn");
		var avenger1 = scn.GetFreepsCard("avenger1");
		var avenger2 = scn.GetFreepsCard("avenger2");
		scn.MoveCardsToHand(pippin);
		scn.MoveCompanionsToTable(huorn, avenger1, avenger2);

		scn.StartGame();

		// Two Ents: Huorn 6 + 2; two Ents are short of the Avengers' 3.
		assertEquals(8, scn.GetStrength(huorn));
		assertEquals(6, scn.GetStrength(avenger1));
		assertEquals(6, scn.GetStrength(avenger2));

		scn.FreepsPlayCard(pippin);
		assertInZone(Zone.FREE_CHARACTERS, pippin);

		// Two Ents and Pippin's 1: Huorn 6 + 3, and the Avengers can spot 3 Ents.
		assertEquals(9, scn.GetStrength(huorn));
		assertEquals(8, scn.GetStrength(avenger1));
		assertEquals(8, scn.GetStrength(avenger2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoEntInPlayPippinMakesTheCountOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var huorn = scn.GetFreepsCard("huorn");
		scn.MoveCardsToHand(pippin);
		scn.MoveCompanionsToTable(huorn);

		scn.StartGame();

		// No Ent: Huorn is his printed 6.
		assertEquals(6, scn.GetStrength(huorn));

		scn.FreepsPlayCard(pippin);
		assertInZone(Zone.FREE_CHARACTERS, pippin);

		// 0 Ents + 1
		assertEquals(7, scn.GetStrength(huorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayersCountOfEntsIsNotRaised() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var avenger = scn.GetFreepsCard("avenger1");
		var urgency = scn.GetShadowCard("urgency");
		scn.MoveCompanionsToTable(pippin, avenger);
		scn.MoveCardsToSupportArea(urgency);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 2 for Ettenmoors + 3 companions + 2 for the one Ent the Shadow player can spot. Pippin's 1 counted for
		// him would make it 4 for Urgency, 9 in all.
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverActionDiscardsTwoPipeweedsFromPlayToRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var toby = scn.GetFreepsCard("toby");
		var ship = scn.GetFreepsCard("ship");
		var leaf = scn.GetFreepsCard("leaf");
		var toby2 = scn.GetFreepsCard("toby2");
		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToSupportArea(toby, ship, leaf);
		scn.AttachCardsTo(pippin, pipe);
		scn.MoveCardsToHand(toby2);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable("troop");

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, toby, ship, leaf);
		assertAttachedTo(pipe, pippin);
		assertInHand(toby2);
		assertTrue(scn.FreepsActionAvailable(pippin));

		scn.FreepsUseCardAction(pippin);
		// Pipeweeds of either culture and either type, in play; not the pipe, nor the pipeweed in hand. Exactly two.
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(toby, ship, leaf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pipe, toby2));
		scn.FreepsChooseCards(toby, ship);

		assertInDiscard(toby, ship);
		assertInZone(Zone.SUPPORT, leaf);
		assertAttachedTo(pipe, pippin);
		assertInHand(toby2);
		// "Remove a burden": one of two.
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyOnePipeweedInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var leaf = scn.GetFreepsCard("leaf");
		var toby2 = scn.GetFreepsCard("toby2");
		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToSupportArea(leaf);
		scn.AttachCardsTo(pippin, pipe);
		scn.MoveCardsToHand(toby2);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable("troop");

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// One pipeweed in play, a second in hand and a pipe in play: two cannot be discarded from play.
		assertInZone(Zone.SUPPORT, leaf);
		assertAttachedTo(pipe, pippin);
		assertInHand(toby2);
		assertEquals(2, scn.GetBurdens());
		assertFalse(scn.FreepsActionAvailable(pippin));
	}
}
