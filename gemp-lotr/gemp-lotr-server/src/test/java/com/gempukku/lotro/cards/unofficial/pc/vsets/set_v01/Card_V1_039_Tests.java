
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("crown", "101_39");
					put("crown2", "101_39");
					put("nazgul1", "1_232");
					put("nazgul2", "1_232");
					put("twigul1", "2_84");
					put("twigul2", "2_84");

					put("arwen", "1_30");
					put("gwemegil", "1_47");
					put("gimli", "1_13");
					put("axe", "1_14");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PaleCrownStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: Pale Crown
		* Side: Free Peoples
		* Culture: ringwraith
		* Twilight Cost: 1
		* Type: artifact
		* Subtype: Helm
		* Vitality: 1
		* Game Text: Plays to your support area.
		* 	If bearer is twilight, characters skirmishing bearer do not receive strength or damage bonuses from cards they bear.
		* 	Skirmish: Remove (1) to transfer this artifact from your support area to a Nazgul.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl crown = scn.GetFreepsCard("crown");

		assertFalse(crown.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, crown.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, crown.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, crown.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, crown.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(crown, Keyword.SUPPORT_AREA));
		assertEquals(PossessionClass.HELM, crown.getBlueprint().getPossessionClasses().stream().findFirst().get()); // test for keywords as needed
		assertEquals(1, crown.getBlueprint().getTwilightCost());
		//assertEquals(, crown.getBlueprint().getStrength());
		assertEquals(1, crown.getBlueprint().getVitality());
		//assertEquals(, crown.getBlueprint().getResistance());
		//assertEquals(Signet., crown.getBlueprint().getSignet());
		//assertEquals(, crown.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void TwilightBearerCancelsStrengthAndDamageBonuses() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl arwen = scn.GetFreepsCard("arwen");
		PhysicalCardImpl gwemegil = scn.GetFreepsCard("gwemegil");
		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		PhysicalCardImpl axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.AttachCardsTo(arwen, gwemegil);
		scn.AttachCardsTo(gimli, axe);

		PhysicalCardImpl twigul1 = scn.GetShadowCard("twigul1");
		PhysicalCardImpl twigul2 = scn.GetShadowCard("twigul2");
		PhysicalCardImpl crown = scn.GetShadowCard("crown");
		PhysicalCardImpl crown2 = scn.GetShadowCard("crown2");
		scn.MoveMinionsToTable(twigul1, twigul2);
		scn.AttachCardsTo(twigul1, crown);
		scn.AttachCardsTo(twigul2, crown2);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		assertEquals(8, scn.GetStrength(arwen)); // 6 base, +2 sword
		assertTrue(scn.HasKeyword(arwen, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(arwen, Keyword.DAMAGE));

		assertEquals(8, scn.GetStrength(gimli)); // 6 base, +2 axe
		assertTrue(scn.HasKeyword(gimli, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, twigul1}, new PhysicalCardImpl[]{gimli, twigul2});

		scn.FreepsResolveSkirmish(arwen);
		assertEquals(9, scn.GetStrength(arwen)); // 6 base, +3 from ability, no +2 from sword
		assertFalse(scn.HasKeyword(arwen, Keyword.DAMAGE)); // damage bonus from sword negated

		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli)); // 6 base, no +2 from axe
		assertTrue(scn.HasKeyword(gimli, Keyword.DAMAGE)); // damage bonus from base
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE)); // damage bonus from axe negated

		scn.FreepsUseCardAction(gimli);
		assertEquals(8, scn.GetStrength(gimli)); // 6 base, +2 from ability, no +2 from axe
	}

	@Test
	public void NonTwilightBearerDoesNotCancelBonuses() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl arwen = scn.GetFreepsCard("arwen");
		PhysicalCardImpl gwemegil = scn.GetFreepsCard("gwemegil");
		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		PhysicalCardImpl axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.AttachCardsTo(arwen, gwemegil);
		scn.AttachCardsTo(gimli, axe);

		PhysicalCardImpl nazgul1 = scn.GetShadowCard("nazgul1");
		PhysicalCardImpl nazgul2 = scn.GetShadowCard("nazgul2");
		PhysicalCardImpl crown = scn.GetShadowCard("crown");
		PhysicalCardImpl crown2 = scn.GetShadowCard("crown2");
		scn.MoveMinionsToTable(nazgul1, nazgul2);
		scn.AttachCardsTo(nazgul1, crown);
		scn.AttachCardsTo(nazgul2, crown2);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		assertEquals(8, scn.GetStrength(arwen)); // 6 base, +2 sword
		assertTrue(scn.HasKeyword(arwen, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(arwen, Keyword.DAMAGE));

		assertEquals(8, scn.GetStrength(gimli)); // 6 base, +2 axe
		assertTrue(scn.HasKeyword(gimli, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, nazgul1}, new PhysicalCardImpl[]{gimli, nazgul2});

		scn.FreepsResolveSkirmish(arwen);
		assertEquals(11, scn.GetStrength(arwen)); // 6 base, +3 from ability, +2 from sword not negated
		assertTrue(scn.HasKeyword(arwen, Keyword.DAMAGE)); // damage bonus from sword not negated
		assertEquals(1, scn.GetKeywordCount(arwen, Keyword.DAMAGE));

		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(gimli);
		assertEquals(8, scn.GetStrength(gimli)); // 6 base, +2 from axe not negated
		assertTrue(scn.HasKeyword(gimli, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE)); // damage bonus from axe not negated

		scn.FreepsUseCardAction(gimli);
		assertEquals(10, scn.GetStrength(gimli)); // 6 base, +2 from ability, +2 from axe not negated
	}

	@Test
	public void SkirmishAbilityPays1ToTransferToNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();

		PhysicalCardImpl nazgul1 = scn.GetShadowCard("nazgul1");
		PhysicalCardImpl twigul1 = scn.GetShadowCard("twigul1");
		PhysicalCardImpl crown = scn.GetShadowCard("crown");
		PhysicalCardImpl crown2 = scn.GetShadowCard("crown2");
		scn.MoveMinionsToTable(nazgul1, twigul1);
		scn.MoveCardsToHand(crown, crown2);

		scn.StartGame();

		scn.SetTwilight(10);

		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(crown);
		assertEquals(Zone.SUPPORT, crown.getZone());
		scn.ShadowPlayCard(crown2);
		assertEquals(Zone.SUPPORT, crown2.getZone());

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowAssignToMinions(frodo, nazgul1, twigul1);

		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(2, scn.GetVitality(nazgul1));
		assertEquals(3, scn.GetVitality(twigul1));

		assertEquals(11, scn.GetTwilight());

		assertTrue(scn.ShadowActionAvailable(crown));
		scn.ShadowUseCardAction(crown);
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to transfer to"));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(nazgul1);
		assertEquals(3, scn.GetVitality(nazgul1));

		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(crown2));
		scn.ShadowUseCardAction(crown2);
		assertEquals(9, scn.GetTwilight());
		assertEquals(Zone.ATTACHED, crown2.getZone());
		assertEquals(4, scn.GetVitality(twigul1));

	}
}
