package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_270_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eowyn", "4_270");

					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound Dwarf companion, strength 6, damage +1
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					put("guma", "4_277");       // Guma, Plains Farmer: an ally; a fellowship ability used only on purpose

					put("slayer", "3_93");      // Morgul Slayer: "Regroup: Exert this minion to wound a companion
					put("slayer2", "3_93");     //   (except the Ring-bearer)."
					put("assassin", "2_93");    // Tower Assassin: "Maneuver: Exert Tower Assassin to wound an ally."
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EowynStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Éowyn, Lady of Rohan
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: <b>Valiant</b>.
		 * 	<b>Response:</b> If an unbound companion is about to take a wound, exert Éowyn and add (1) to prevent that wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eowyn");

		assertEquals("Éowyn", card.getBlueprint().getTitle());
		assertEquals("Lady of Rohan", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, card.getBlueprint().getSignet());
	}

	@Test
	public void ExertsAndAddsOneToPreventAWoundToAnUnboundCompanionHerselfIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(eowyn, gimli);

		var slayer = scn.GetShadowCard("slayer");
		var slayer2 = scn.GetShadowCard("slayer2");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer, slayer2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(gimli);

		// Another unbound companion.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsChooseOptionalTrigger(eowyn);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Eowyn herself: the exertion is no wound, and the wound is prevented.
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer2);
		scn.ShadowChooseCard(eowyn);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsChooseOptionalTrigger(eowyn);
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileEowynIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(eowyn, gimli);

		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.AddWoundsToChar(eowyn, 2);
		assertEquals(2, scn.GetWoundsOn(eowyn));
		scn.MoveMinionsToTable(slayer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		// Vitality 3 with 2 wounds: she cannot exert.
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(gimli);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedForAWoundToAnAllyAMinionOrARingBoundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(eowyn, gimli, sam);
		scn.MoveAlliesToTable(guma);

		var assassin = scn.GetShadowCard("assassin");
		var savage = scn.GetShadowCard("savage");
		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, savage, slayer);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// An ally: Guma is the only one, so the Tower Assassin wounds him.
		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		scn.ShadowUseCardAction(assassin);
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// A minion: 6 against 5, Gimli wins, and with his damage +1 the Savage takes 2 wounds.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(pool, scn.GetTwilight());

		// A Ring-bound companion.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(sam);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
