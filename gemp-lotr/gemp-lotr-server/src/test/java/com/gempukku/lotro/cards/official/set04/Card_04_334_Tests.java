package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_334_Tests
{

	// The Towers path with Rohirrim Village as its site 2.  Site 1 is Eastemnet Gullies (a Fellowship action that
	// plays Legolas from the draw deck: used only on purpose) and site 3 Barrows of Edoras (a sanctuary whose
	// Fellowship action is used only on purpose), not Westemnet Plains 4_342, whose text is the Village's.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, the fellowship's one archer; his archery ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a Maneuver ability used only on purpose
					put("defend", "4_115");     // Defend It and Hope: [gondor] Maneuver event, exert a [gondor] Man to wound a minion (twice if an Uruk-hai)

					put("band", "4_4");         // Band of Wild Men: [dunland] Man minion, vitality 2; a trigger on its winning only
					put("chief", "4_20");       // Hill Chief: [dunland] Man minion, vitality 2; a Shadow ability that needs a site he controls
					put("saruman", "4_33");     // Saruman, Rabble-rouser: a [dunland] minion who is no Man (a Wizard); laid after the move, which his trigger answers
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man minion, vitality 2; strength +3 and fierce only at 2 burdens
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");
					put("site2", "4_334");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimVillageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Rohirrim Village
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 2T
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: Each [dunland] Man may not take wounds during the archery phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Rohirrim Village", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void AtTheVillageTheFellowshipArcheryWoundCannotBePlacedOnADunlandMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var band = scn.GetShadowCard("band");
		var chief = scn.GetShadowCard("chief");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.FreepsPass();
		assertEquals("Rohirrim Village", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(band, chief, saruman, axeman);

		scn.SkipToArcheryWounds();
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(band));
		assertEquals(0, scn.GetWoundsOn(chief));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(axeman));

		// The two [dunland] Men cannot take it; the [dunland] Wizard and the [raider] Man can.
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, axeman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(band, chief));
		scn.ShadowChooseCard(axeman);

		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(band));
		assertEquals(0, scn.GetWoundsOn(chief));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AtTheVillageADunlandManTakesAWoundInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		var band = scn.GetShadowCard("band");
		var chief = scn.GetShadowCard("chief");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.FreepsPass();
		assertEquals("Rohirrim Village", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(band, chief);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(band));
		assertEquals(0, scn.GetWoundsOn(chief));
		assertTrue(scn.FreepsPlayAvailable(defend));

		// Aragorn, the one [gondor] Man, exerts unasked; the wound may go on a [dunland] Man outside the archery phase.
		scn.FreepsPlayCard(defend);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(band, chief));
		scn.FreepsChooseCard(band);

		assertEquals(1, scn.GetWoundsOn(band));
		assertEquals(0, scn.GetWoundsOn(chief));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftTheVillageADunlandManTakesAnArcheryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var band = scn.GetShadowCard("band");
		var chief = scn.GetShadowCard("chief");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.FreepsPass();
		assertEquals("Barrows of Edoras", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(band, chief, axeman);

		scn.SkipToArcheryWounds();
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(band));
		assertEquals(0, scn.GetWoundsOn(chief));
		assertEquals(0, scn.GetWoundsOn(axeman));

		// The Village lies behind: its text no longer applies, and every minion may take the wound.
		assertTrue(scn.ShadowHasCardChoiceAvailable(band, chief, axeman));
		scn.ShadowChooseCard(band);

		assertEquals(1, scn.GetWoundsOn(band));
		assertEquals(0, scn.GetWoundsOn(chief));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
