package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ted", "18_75");

					put("legion", "11_88");     // Legion of Harad: a [men] Man minion, strength 14, vitality 3, with no text but toil
					put("corps", "11_73");      // Corps of Harad: a [men] Man minion, strength 10, cost 4; fierce only at a battleground, and this path has none
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion with no text, strength 8
					// Courageous Easterling: [men] Man, strength 5.  "Skirmish: If this minion is skirmishing, discard this minion
					// to play a [men] minion from your discard pile.  That minion is fierce until the regroup phase."
					put("courageous", "15_75");
					put("trident", "12_77");    // War Trident: a [men] possession for a [men] minion; its bonuses hold only in a skirmish
					put("fleet", "18_66");      // Fleet of Corsair Ships: a [men] support-area possession; its ability needs 4 tokens it has not got
					put("lying", "13_95");      // Lying in Wait: a [men] support-area condition; its texts need a bearer or a skirmish

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf, strength 6, vitality 3; his ability is used only on purpose
					put("armor", "1_8");        // Dwarven Armor: a Free Peoples possession for a Dwarf, which changes no number
					put("artifice", "17_4");    // Ring of Artifice: a Dwarf's artifact; damage +1, and no strength with no [dwarven] artifact discarded
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; his ability is used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TedSandymanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Ted Sandyman, Chief's Men's Ally
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Site Number: 2
		 * Game Text: <b>Maneuver:</b> Liberate a site you control to make each [men] minion strength +2 until the regroup phase.
		 * <b>Regroup:</b> Exert Ted Sandyman to stack a [men] Man in play on a possession.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ted");

		assertEquals("Ted Sandyman", card.getBlueprint().getTitle());
		assertEquals("Chief's Men's Ally", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityLiberatesTheSiteHeControlsNotTheOpponentsToMakeEachMenMinionStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ted = scn.GetShadowCard("ted");
		var legion = scn.GetShadowCard("legion");
		var brigand = scn.GetShadowCard("brigand");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(4);
		// Sites 1 to 3 are behind both markers: site 1 to the Shadow player, site 2 to the Free Peoples player.
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(site1);
		scn.FreepsTakeControlOfSite(site2);
		scn.MoveMinionsToTable(ted, legion, brigand);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site2));
		assertEquals(3, scn.GetStrength(ted));
		assertEquals(14, scn.GetStrength(legion));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.ShadowActionAvailable(ted));

		scn.ShadowUseCardAction(ted);
		// His own site goes back on the path; the opponent's higher site stays where it was.
		assertFalse(scn.ShadowControls(site1));
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertTrue(scn.FreepsControls(site2));
		// Each [men] minion, the Hobbit and the Man: 3 + 2 and 14 + 2.  Not the [dunland] Man, nor a companion.
		assertEquals(5, scn.GetStrength(ted));
		assertEquals(16, scn.GetStrength(legion));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithTwoSitesOfHisOwnHeChoosesWhichIsLiberated() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.MoveMinionsToTable(ted);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));

		scn.ShadowUseCardAction(ted);
		// Both of his own sites are offered, and the one he chooses goes back on the path, the lower one here
		// (Ketura's ruling on Q-0370: "a site you control" is his choice).
		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(site1);
		assertFalse(scn.ShadowControls(site1));
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertTrue(scn.ShadowControls(site2));
		assertEquals(5, scn.GetStrength(ted));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhenOnlyTheOpponentControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ted = scn.GetShadowCard("ted");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.FreepsTakeControlOfSite(site1);
		scn.MoveMinionsToTable(ted, legion);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ted));
		assertEquals(14, scn.GetStrength(legion));
	}

	@Test
	public void TheBonusHoldsInASecondSkirmishAndIsGoneAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ted = scn.GetShadowCard("ted");
		var legion = scn.GetShadowCard("legion");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.MoveMinionsToTable(ted, legion);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(3, scn.GetStrength(ted));
		assertEquals(14, scn.GetStrength(legion));
		scn.ShadowUseCardAction(ted);
		assertEquals(5, scn.GetStrength(ted));
		assertEquals(16, scn.GetStrength(legion));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, legion}, new PhysicalCardImpl[]{aragorn, ted});
		scn.FreepsResolveSkirmish(gimli);
		// Legion 16 against Gimli 6: overwhelmed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, gimli);

		// The second skirmish is a phase of its own: the bonus still holds.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(ted));
		assertEquals(16, scn.GetStrength(legion));

		// Ted 5 loses to Aragorn 8 and takes a wound; then the regroup phase, and the bonus is gone.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(ted));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetStrength(ted));
		assertEquals(14, scn.GetStrength(legion));
	}

	@Test
	public void AMenMinionThatComesIntoPlayAfterTheUseIsNotMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ted = scn.GetShadowCard("ted");
		var courageous = scn.GetShadowCard("courageous");
		var corps = scn.GetShadowCard("corps");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.MoveMinionsToTable(ted, courageous);
		scn.MoveCardsToDiscard(corps);
		assertInDiscard(corps);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(3, scn.GetStrength(ted));
		assertEquals(5, scn.GetStrength(courageous));
		scn.ShadowUseCardAction(ted);
		assertEquals(5, scn.GetStrength(ted));
		// 5 + 2
		assertEquals(7, scn.GetStrength(courageous));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, courageous);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// Enough for the Corps: 4, and no roaming at site 5.
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The Easterling, skirmishing, is discarded to play the Corps from the discard pile.
		scn.ShadowUseCardAction(courageous);
		assertInZone(Zone.SHADOW_CHARACTERS, corps);
		assertInDiscard(courageous);
		// Printed strength: the ability reached only the [men] minions on the table when it was used.
		assertEquals(10, scn.GetStrength(corps));
		assertEquals(5, scn.GetStrength(ted));
		// Aragorn's skirmish has no minion left, and the Corps is fierce: the fierce assignments follow.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsTedToStackAMenManInPlayOnAPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ted = scn.GetShadowCard("ted");
		var legion = scn.GetShadowCard("legion");
		var corps = scn.GetShadowCard("corps");
		var brigand = scn.GetShadowCard("brigand");
		var trident = scn.GetShadowCard("trident");
		var fleet = scn.GetShadowCard("fleet");
		var lying = scn.GetShadowCard("lying");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var artifice = scn.GetFreepsCard("artifice");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor, artifice);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// Laid at the start of the regroup phase, where minions that lived through the skirmishes stand.
		scn.MoveMinionsToTable(ted, legion, corps, brigand);
		scn.AttachCardsTo(corps, trident);
		scn.MoveCardsToSupportArea(fleet, lying);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(artifice, gimli);
		assertAttachedTo(trident, corps);
		assertInZone(Zone.SUPPORT, fleet, lying);
		assertEquals(0, scn.GetWoundsOn(ted));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(ted));

		scn.ShadowUseCardAction(ted);
		assertEquals(1, scn.GetWoundsOn(ted));
		// Any possession, borne by either side or in a support area; not an artifact, nor a condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, trident, fleet));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(artifice, lying));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(armor);

		// A [men] Man of either title; not Ted, a [men] Hobbit, nor the [dunland] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(legion, corps));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ted, brigand));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(legion);

		assertInZone(Zone.STACKED, legion);
		assertTrue(scn.GetStackedCards(armor).contains(legion));
		assertEquals(0, scn.GetStackedCards(trident).size());
		assertInZone(Zone.SHADOW_CHARACTERS, corps);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileTedIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ted = scn.GetShadowCard("ted");
		var legion = scn.GetShadowCard("legion");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(ted, legion);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(ted, 3);
		assertEquals(3, scn.GetWoundsOn(ted));
		assertAttachedTo(armor, gimli);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ted));
		assertInZone(Zone.SHADOW_CHARACTERS, legion);
	}

	@Test
	public void WithNoMenManInPlayTheRegroupAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(ted, brigand);
		assertEquals(0, scn.GetWoundsOn(ted));
		assertAttachedTo(armor, gimli);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(ted));

		scn.ShadowUseCardAction(ted);
		assertEquals(1, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetStackedCards(armor).size());
		assertInZone(Zone.SHADOW_CHARACTERS, ted, brigand);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoPossessionInPlayTheRegroupAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ted = scn.GetShadowCard("ted");
		var legion = scn.GetShadowCard("legion");
		var lying = scn.GetShadowCard("lying");
		var gimli = scn.GetFreepsCard("gimli");
		var artifice = scn.GetFreepsCard("artifice");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, artifice);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(ted, legion);
		scn.MoveCardsToSupportArea(lying);
		assertEquals(0, scn.GetWoundsOn(ted));
		assertAttachedTo(artifice, gimli);
		assertInZone(Zone.SUPPORT, lying);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(ted));

		scn.ShadowUseCardAction(ted);
		assertEquals(1, scn.GetWoundsOn(ted));
		// An artifact and a condition are no possessions: Legion stays in play.
		assertInZone(Zone.SHADOW_CHARACTERS, legion);
		assertEquals(0, scn.GetStackedCards(artifice).size());
		assertEquals(0, scn.GetStackedCards(lying).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
