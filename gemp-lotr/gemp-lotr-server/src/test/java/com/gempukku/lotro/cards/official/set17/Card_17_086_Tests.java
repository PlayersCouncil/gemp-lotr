package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_086_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("veteran", "17_86");

					put("denizen", "11_115");     // Denizen of Khazad-dum: [orc] Orc, strength 9; +2 only at an underground site
					put("vicious", "17_92");      // Vicious Warg: [orc] mount, +4 strength, fierce; bearer an [orc] Orc of strength 9 or less
					// Threatening Warg: [orc] mount, +2 strength, fierce; bearer of strength 10 or less.  Its play trigger
					// wants 2 [orc] Orcs bearing no mount, never the case when it is played here.
					put("threatening", "17_91");
					put("gear", "17_90");         // Rider's Gear: an [orc] possession that is no mount (armor); a wound bar while mounted
					put("warwarg", "5_64");       // War-warg: an [isengard] mount, for a warg-rider; a skirmish response
					put("wargrider", "5_67");     // Warg-rider: [isengard] warg-rider Orc; fierce while not exhausted
					// Always Threatening: [orc] support-area condition, the one card that takes [orc] tokens by its text;
					// its skirmish ability is never used.
					put("always", "13_103");
					put("always2", "13_103");
					// Ships of Great Draught: [raider] support-area possession that takes [raider] tokens; its abilities are never used.
					put("ships", "8_65");

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a maneuver ability only
					put("sam", "1_311");          // Sam, Son of Hamfast: vitality 4; his abilities are never used
					put("barliman", "1_70");      // Barliman Butterbur: an ally; a fellowship ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishVeteranStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Veteran
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: Each time you play an [orc] mount, you may reinforce an [orc] token.<br><b>Regroup:</b> Exert Orkish Veteran to make the Free Peoples player wound a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("veteran");

		assertEquals("Orkish Veteran", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachOrcMountPlayedOffersToReinforceAnOrcTokenAndNoOtherPossessionOrMountDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var denizen = scn.GetShadowCard("denizen");
		var vicious = scn.GetShadowCard("vicious");
		var threatening = scn.GetShadowCard("threatening");
		var gear = scn.GetShadowCard("gear");
		var warwarg = scn.GetShadowCard("warwarg");
		var wargrider = scn.GetShadowCard("wargrider");
		var always = scn.GetShadowCard("always");
		var always2 = scn.GetShadowCard("always2");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(veteran, vicious, threatening, gear, warwarg);
		scn.MoveCardsToSupportArea(always, always2, ships);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, wargrider);
		// An [orc] token on each Always Threatening, a [raider] token on the Ships.
		scn.AddTokensToCard(always, 1);
		scn.AddTokensToCard(always2, 1);
		scn.AddTokensToCard(ships, 1);
		assertInHand(veteran, vicious, threatening, gear, warwarg);
		assertEquals(1, scn.GetCultureTokensOn(always));
		assertEquals(1, scn.GetCultureTokensOn(always2));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		// Its own play: an [orc] card that is no mount.
		scn.ShadowPlayCard(veteran);
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetCultureTokensOn(always));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(vicious);
		scn.ShadowChooseCard(veteran);
		assertAttachedTo(vicious, veteran);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(veteran));
		scn.ShadowAcceptOptionalTrigger();

		// Any card with an [orc] token on it, not one with a token of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(always, always2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ships));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(always);
		assertEquals(2, scn.GetCultureTokensOn(always));
		assertEquals(1, scn.GetCultureTokensOn(always2));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second [orc] mount, of another title, on the only Orc of strength 10 or less: offered again.
		scn.ShadowPlayCard(threatening);
		assertAttachedTo(threatening, denizen);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(veteran));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(always2);
		assertEquals(2, scn.GetCultureTokensOn(always));
		assertEquals(2, scn.GetCultureTokensOn(always2));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// An [orc] possession that is no mount.
		scn.ShadowPlayCard(gear);
		scn.ShadowChooseCard(denizen);
		assertAttachedTo(gear, denizen);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A mount of another culture.
		scn.ShadowPlayCard(warwarg);
		assertAttachedTo(warwarg, wargrider);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetCultureTokensOn(always));
		assertEquals(2, scn.GetCultureTokensOn(always2));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheReinforcementLeavesTheTokensAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var vicious = scn.GetShadowCard("vicious");
		var always = scn.GetShadowCard("always");
		scn.MoveCardsToHand(vicious);
		scn.MoveCardsToSupportArea(always);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran);
		scn.AddTokensToCard(always, 1);
		assertInHand(vicious);
		assertEquals(1, scn.GetCultureTokensOn(always));
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(vicious);
		assertAttachedTo(vicious, veteran);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(veteran));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetCultureTokensOn(always));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOrcTokenInPlayTheOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var vicious = scn.GetShadowCard("vicious");
		var always = scn.GetShadowCard("always");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(vicious);
		// An [orc] card with no token, and a token of another culture.
		scn.MoveCardsToSupportArea(always, ships);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran);
		scn.AddTokensToCard(ships, 1);
		assertInHand(vicious);
		assertEquals(0, scn.GetCultureTokensOn(always));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(vicious);
		assertAttachedTo(vicious, veteran);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(always));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsTheVeteranToMakeTheFreePeoplesPlayerWoundACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(barliman);
		// Sam is exhausted: 3 wounds on 4 vitality.
		scn.AddWoundsToChar(sam, 3);

		var veteran = scn.GetShadowCard("veteran");

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(sam));
		scn.MoveMinionsToTable(veteran);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(veteran));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(veteran));
		scn.ShadowUseCardAction(veteran);

		assertEquals(1, scn.GetWoundsOn(veteran));
		// The Free Peoples player chooses: any companion, the exhausted one too; not an ally, not a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, sam, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, veteran));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);

		// A wound and not an exertion: the exhausted Sam takes it and is killed.
		assertInZone(Zone.DEAD, sam);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileTheVeteranIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var veteran = scn.GetShadowCard("veteran");

		scn.StartGame();
		scn.MoveMinionsToTable(veteran);
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(veteran, 3);
		assertEquals(3, scn.GetWoundsOn(veteran));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(veteran));
	}
}
