package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_025_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("jarnsmid", "2_25");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
					put("rider", "4_265");      // Elite Rider: a [rohan] Man; his texts read his starting fellowship and his skirmish

					put("shield", "1_107");     // Great Shield: [gondor] possession, shield, twilight 1, on a Man
					put("helm", "5_89");        // Rohirrim Helm: [rohan] possession, helm, twilight 1, on a [rohan] Man
					put("coat", "2_105");       // Mithril-coat: [shire] ARTIFACT, armor, twilight 2, on Frodo
					put("glamdring", "1_75");   // Glamdring: [gandalf] possession, hand weapon, twilight 2, on Gandalf
					put("bow", "1_90");         // Aragorn's Bow: [gondor] possession, RANGED weapon, twilight 1, on Aragorn
					put("pipe", "1_91");        // Aragorn's Pipe: [gondor] possession, pipe, twilight 1, on a [gondor] companion

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability used only on purpose
					put("scimitar", "1_269");   // Orc Scimitar: [sauron] possession, hand weapon, twilight 1, on a [sauron] Orc
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void JarnsmidStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Járnsmid, Merchant from Dale
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: To play, spot Gandalf.
		 * Each time you play a shield, armor, helm, or hand weapon, its twilight cost is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("jarnsmid");

		assertEquals("Járnsmid", card.getBlueprint().getTitle());
		assertEquals("Merchant from Dale", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void CannotBePlayedUntilGandalfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jarnsmid = scn.GetFreepsCard("jarnsmid");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(jarnsmid, gandalf);

		scn.StartGame();

		// The Ent Avenger is a [gandalf] companion, and no Gandalf.
		assertFalse(scn.FreepsPlayAvailable(jarnsmid));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(jarnsmid));

		scn.FreepsPlayCard(jarnsmid);
		assertInZone(Zone.SUPPORT, jarnsmid);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachShieldArmorHelmAndHandWeaponPlayedCostsOneLessAndARangedWeaponOrPipeDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jarnsmid = scn.GetFreepsCard("jarnsmid");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		var frodo = scn.GetRingBearer();
		var shield = scn.GetFreepsCard("shield");
		var helm = scn.GetFreepsCard("helm");
		var coat = scn.GetFreepsCard("coat");
		var glamdring = scn.GetFreepsCard("glamdring");
		var bow = scn.GetFreepsCard("bow");
		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveAlliesToTable(jarnsmid);
		scn.MoveCompanionsToTable(gandalf, aragorn, rider);
		scn.MoveCardsToHand(shield, helm, coat, glamdring, bow, pipe);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		// A [gondor] shield: 1 - 1. Aragorn and the Rider are both Men.
		scn.FreepsPlayCard(shield);
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(shield, aragorn);
		assertEquals(0, scn.GetTwilight());
		// A [rohan] helm: 1 - 1.
		scn.FreepsPlayCard(helm);
		assertAttachedTo(helm, rider);
		assertEquals(0, scn.GetTwilight());
		// A [shire] armor that is an artifact: 2 - 1, where a discount of 2 would read 0.
		scn.FreepsPlayCard(coat);
		assertAttachedTo(coat, frodo);
		assertEquals(1, scn.GetTwilight());
		// A [gandalf] hand weapon: 2 - 1.
		scn.FreepsPlayCard(glamdring);
		assertAttachedTo(glamdring, gandalf);
		assertEquals(2, scn.GetTwilight());

		// A ranged weapon and a pipe pay their printed 1 each.
		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, aragorn);
		assertEquals(3, scn.GetTwilight());
		scn.FreepsPlayCard(pipe);
		assertAttachedTo(pipe, aragorn);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHandWeaponTheShadowPlayerPlaysCostsHimItsPrintedTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jarnsmid = scn.GetFreepsCard("jarnsmid");
		var soldier = scn.GetShadowCard("soldier");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveAlliesToTable(jarnsmid);
		scn.MoveCardsToShadowHand("scimitar");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		int before = scn.GetTwilight();
		scn.ShadowPlayCard(scimitar);
		assertAttachedTo(scimitar, soldier);
		// The Shadow player removes the Scimitar's printed 1: Járnsmid's discount is for his own player's plays.
		assertEquals(before - 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
