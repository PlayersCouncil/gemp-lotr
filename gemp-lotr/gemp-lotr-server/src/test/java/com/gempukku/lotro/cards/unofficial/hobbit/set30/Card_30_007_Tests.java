package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_007_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 ([shire] Hobbit) as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bombur", "30_7");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, vitality 3, no damage bonus; a fellowship ability only
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] Dwarf companion, vitality 3, no damage bonus; a maneuver ability only
					// Dain Ironfoot, Lord of the Iron Hills: [dwarven] Dwarf ally, vitality 3 (he can exert); a
					// bonus for Thorin and a regroup offer that wants a [dwarven] follower to discard
					put("dain", "32_2");
					// Wicked Spider: [spider] Spider; here only a minion, so that there is a maneuver phase
					put("spider", "31_55");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BomburStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Bombur, Chief Cook
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 3
		 * Type: Follower
		 * Subtype: Dwarf
		 * Strength: +2
		 * Aid: Exert a [dwarven] companion.
		 * Game Text: <b>Aid</b> – Exert a [dwarven] companion. <helper>(At the start of the maneuver phase, you may exert a [dwarven] companion to transfer this to a companion.)</helper>
		 * 	Bearer is <b>damage +1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bombur");

		assertEquals("Bombur", card.getBlueprint().getTitle());
		assertEquals("Chief Cook", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void AidExertsADwarvenCompanionToTransferBomburWhoMakesHisBearerDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var bombur = scn.GetFreepsCard("bombur");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(balin, nori);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(bombur);

		scn.StartGame();
		scn.MoveMinionsToTable("spider");
		scn.SkipToPhase(Phase.MANEUVER);

		// In the support area Bombur has no bearer: nobody is damage +1.
		assertInZone(Zone.SUPPORT, bombur);
		assertEquals(0, scn.GetKeywordCount(balin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(nori, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(nori));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bombur, "Aid"));

		scn.FreepsAcceptOptionalTrigger();
		// Two [dwarven] companions may pay; Bilbo, a [shire] companion, and Dain, a [dwarven] ally who could exert,
		// may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, nori));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, dain));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(nori);

		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(nori));
		assertEquals(0, scn.GetWoundsOn(dain));

		scn.FreepsChooseCard(balin);
		assertAttachedTo(bombur, balin);
		assertEquals(1, scn.GetKeywordCount(balin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(nori, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(bilbo, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AidIsNotOfferedWhileTheOnlyDwarvenCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var bombur = scn.GetFreepsCard("bombur");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(bombur);
		// Vitality 3 with 2 wounds: Balin cannot exert. Bilbo and Dain can, but are no [dwarven] companions.
		scn.AddWoundsToChar(balin, 2);

		scn.StartGame();
		scn.MoveMinionsToTable("spider");

		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(dain));

		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bombur, "Aid"));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.SUPPORT, bombur);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(dain));
	}
}
