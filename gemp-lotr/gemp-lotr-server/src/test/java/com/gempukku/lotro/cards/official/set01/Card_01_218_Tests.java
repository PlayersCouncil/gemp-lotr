package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_218_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "1_218");
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, strength 9, vitality 2; no damage bonus,
					                            // and her text reaches only a character bearing a [wraith] condition
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl (his texts want 3 and 5 burdens)
					put("attea", "2_82");       // Úlairë Attëa, The Easterling: a twilight [wraith] Nazgûl (cheated in, so his play trigger never fires)
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, no Nazgûl (his text wants 2 wounds on the Ring-bearer)
					put("found", "2_78");       // It Wants to be Found: [wraith] Maneuver event, spot a twilight Nazgûl to add a burden
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NazgulSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Nazgûl Sword
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be a Nazgûl.
		 * While you can spot 3 burdens, bearer is damage +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Nazgûl Sword", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnANazgulOfAnyCultureAndNotOnAnotherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var sword = scn.GetShadowCard("sword");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul, hound);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(sword));
		scn.ShadowPlayCard(sword);
		// A [wraith] Nazgûl and a [raider] one; not the [wraith] Orc, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, frodo, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);
		assertAttachedTo(sword, khamul);
		assertNotAttachedTo(sword, lemenya);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsDamagePlusOneOnceThreeBurdensCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var sword = scn.GetShadowCard("sword");
		var lemenya = scn.GetShadowCard("lemenya");
		var attea = scn.GetShadowCard("attea");
		var found = scn.GetShadowCard("found");
		scn.MoveCardsToHand(found);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, attea);
		scn.AttachCardsTo(lemenya, sword);
		scn.AddBurdens(2);
		// A wound on the Ring-bearer beside the 2 burdens: a third token on him, and no burden.
		scn.AddWoundsToChar(frodo, 1);
		scn.SetTwilight(10);

		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(attea, Keyword.DAMAGE));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// Spot Attëa, a twilight Nazgûl, to add the third burden.
		scn.ShadowPlayCard(found);
		assertEquals(3, scn.GetBurdens());
		assertEquals(1, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		// Another Nazgûl, not the bearer.
		assertEquals(0, scn.GetKeywordCount(attea, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();

		// Lemenya 9 + 2 for the sword against Aragorn's 8: she wins, and he takes 1 wound for losing + 1 for the sword.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
