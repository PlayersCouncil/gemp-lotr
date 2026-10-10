package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_079_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_79");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					// Aragorn's Bow: a ranged weapon, cost 1.  "Bearer must be Aragorn."
					put("bow", "1_90");

					// What restores Anduril by legal play:
					// Mantle of the White Wizard, [gandalf] condition in the support area: "Each time you
					// play a spell, you may hinder a [gandalf] artifact to restore a Free Peoples card."
					put("mantle", "103_3");
					// Gandalf, Friend of the Shirefolk, bearing Gandalf's Staff, a [gandalf] artifact ("The
					// twilight cost of each [gandalf] spell is -1")
					put("gandalf", "1_72");
					put("staff", "2_22");
					// A New Light: [gandalf] Fellowship event, a spell, cost 1.  To play, spot a [gandalf]
					// Wizard.  It takes a minion from the Shadow player's discard pile, where there is none.
					put("light", "17_15");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AndurilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Andúril, Flame of the West
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Artifact
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Vitality: 1
		 * Game Text: Bearer must be Aragorn. Discard other weapons he bears. He is <b>damage +1</b> and cannot bear other weapons.<br><b>Fellowship</b> <i>or</i> <b>Regroup:</b> If the fellowship is at any site 2 or any site 5, play the fellowship's next site (replacing opponent's site if necessary).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Andúril", card.getBlueprint().getTitle());
		assertEquals("Flame of the West", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	// Comprehensive Rules 5.0, "hinder": "If Anduril is hindered, a Ranger's Sword cannot be played on Aragorn due
	// to passive item class restrictions.  But because Anduril's game text does not apply, Aragorn's Bow can now
	// be played.  Upon being restored, Anduril's game text resumes being applied, upon which Aragorn's Bow would
	// be discarded."  (F-0360.)  Here a card restores it; mechanics/HinderedBorneCardTests has the same board with
	// a reconcile doing the restoring, parked on F-0361.
	@Test
	public void WhenRestoredDiscardsAWeaponAragornTookUpWhileItWasHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("card");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var mantle = scn.GetFreepsCard("mantle");
		var light = scn.GetFreepsCard("light");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.AttachCardsTo(aragorn, anduril);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToSupportArea(mantle);
		scn.MoveCardsToHand(bow, light);

		scn.StartGame();

		// "Cannot bear other weapons"
		assertFalse(scn.FreepsPlayAvailable(bow));

		scn.HinderCard(anduril);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(bow));
		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, aragorn);

		// The spell is played, and the Mantle hinders the Staff (the one [gandalf] artifact) to restore Anduril
		scn.FreepsPlayCard(light);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mantle));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.IsHindered(staff));
		assertTrue(scn.FreepsHasCardChoiceAvailable(anduril));
		scn.FreepsChooseCard(anduril);

		assertFalse(scn.IsHindered(anduril));
		assertAttachedTo(anduril, aragorn);
		assertInZone(Zone.DISCARD, bow);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
