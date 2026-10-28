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

public class Card_07_279_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "7_279");
					put("engineer", "7_272");   // Gorgoroth Engineer: a besieger Orc; his text answers only his play from a stacked site
					put("troll", "8_102");      // Great Hill Troll: a besieger who is a Troll; his trigger is for King-block sites only
					// Uruk Besieger: [isengard] Uruk-hai with "Besieger" in the title and not the keyword; damage +1, and a
					// regroup ability that stacks himself
					put("uruk", "4_180");
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc who is no besieger; his text answers a weapon played on him

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothTroopStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gorgoroth Troop
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.<br><b>Skirmish:</b> Stack your besieger on a site you control to make Gorgoroth Troop strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troop");

		assertEquals("Gorgoroth Troop", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityStacksABesiegerOnASiteHeControlsToMakeHimStrengthPlus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var engineer = scn.GetShadowCard("engineer");
		var troll = scn.GetShadowCard("troll");
		var uruk = scn.GetShadowCard("uruk");
		var chieftain = scn.GetShadowCard("chieftain");

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.SkipToSite(4);
		// Site 1 to the Free Peoples player, sites 2 and 3 to the Shadow player.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.ShadowControls(site3));

		scn.MoveMinionsToTable(troop, engineer, troll, uruk, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, uruk}, new PhysicalCardImpl[]{gimli, chieftain});
		// The Troop, the Engineer and the Troll are left out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The ability is his though he is in no skirmish.
		assertEquals(14, scn.GetStrength(troop));
		assertTrue(scn.ShadowActionAvailable(troop));
		scn.ShadowUseCardAction(troop);

		// His two sites, not the Free Peoples player's.
		assertTrue(scn.ShadowHasCardChoiceAvailable(site2, site3));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site1));
		scn.ShadowChooseCard(site2);

		// Any besieger of his, the Troop himself among them (no "another") and one who is a Troll; not the Uruk-hai
		// with the word in his title only, nor the [sauron] Orc who is no besieger.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, engineer, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(uruk, chieftain));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(engineer);

		assertInZone(Zone.STACKED, engineer);
		assertTrue(scn.GetStackedCards(site2).contains(engineer));
		assertTrue(scn.GetStackedCards(site3).isEmpty());
		assertInZone(Zone.SHADOW_CHARACTERS, troop, troll, uruk, chieftain);
		assertEquals(16, scn.GetStrength(troop));

		// Still +2 at the next decision of that skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(16, scn.GetStrength(troop));

		// Gone in the next skirmish.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(14, scn.GetStrength(troop));
	}

	@Test
	public void WithNoSiteOfHisOwnTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var engineer = scn.GetShadowCard("engineer");

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		// The one site controlled is the Free Peoples player's.
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.MoveMinionsToTable(troop, engineer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Two besiegers of his in play, the Troop in his own skirmish, and nowhere to stack either.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troop));
		assertEquals(14, scn.GetStrength(troop));
	}
}
