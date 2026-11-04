package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_092_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("overrun", "11_92");
					// Harad Standard-bearer and Squad of Haradrim: [men] Man minions, strength 10 and 9.  Their
					// abilities (a skirmish one that removes (3), a regroup one) are never used.
					put("bearer", "11_84");
					put("squad", "11_99");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit.  His maneuver ability wants a
					// site he controls (none is); his regroup ability is never reached.
					put("ted", "18_75");
					// Dunlending Brigand: a [dunland] Man minion with no text, strength 8.
					put("brigand", "4_10");
					// Lying in Wait: a [men] condition in the support area.  Its skirmish ability is never used, and
					// its other two sentences read only once it is borne.
					put("lying", "13_95");

					// Gimli, Son of Glóin and Aragorn, Ranger of the North: companions to skirmish.  Their abilities
					// are never used.
					put("gimli", "1_13");
					put("aragorn", "1_89");
				}},
				new HashMap<>()
				{{
					// Site 2 is Ettenmoors 1_331, a plains and nothing more.  Its skirmish ability is never used.
					putAll(VirtualTableScenario.FellowshipSites);
					// Stables: a site with no terrain.  Its start-of-fellowship trigger is never reached.
					put("stables", "11_259");
					// Fortress of Orthanc: a battleground and no plains.  Its move trigger adds (2) for each
					// companion over 5, and the fellowships here have at most three.
					put("fortress", "11_241");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OverrunStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Overrun
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [men] minion strength +2 (or +4 if that minion is at a battleground or plains site).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("overrun");

		assertEquals("Overrun", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AwayFromABattlegroundOrPlainsItMakesAMenMinionStrengthPlus2UntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		var overrun = scn.GetShadowCard("overrun");
		var bearer = scn.GetShadowCard("bearer");
		var squad = scn.GetShadowCard("squad");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		var lying = scn.GetShadowCard("lying");
		scn.MoveCardsToHand(overrun);
		scn.MoveCardsToSupportArea(lying);

		scn.StartGame();
		assertInHand(overrun);
		assertInZone(Zone.SUPPORT, lying);
		scn.MoveCardToAdventurePath(scn.GetShadowSite("stables"));
		scn.FreepsPass();
		scn.MoveMinionsToTable(bearer, squad, ted, brigand);

		assertEquals("Stables", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, bearer},
				new PhysicalCardImpl[]{aragorn, squad});
		// The Shadow player puts the Brigand beside the Standard-bearer, and leaves Ted unassigned.
		scn.ShadowAssignToMinions(gimli, brigand);
		assertTrue(scn.IsCharAssignedAgainst(gimli, brigand));
		assertFalse(scn.IsCharAssigned(ted));
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertEquals(10, scn.GetStrength(bearer));
		assertEquals(9, scn.GetStrength(squad));
		assertEquals(3, scn.GetStrength(ted));
		assertTrue(scn.ShadowPlayAvailable(overrun));
		scn.ShadowPlayCard(overrun);

		// A [men] minion in this skirmish, one assigned to a skirmish still to come and one assigned to none; not
		// the [dunland] Man in this skirmish, nor the [men] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bearer, squad, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lying));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(squad);

		// 9 + 2; +4 would read 13.
		assertEquals(11, scn.GetStrength(squad));
		assertEquals(10, scn.GetStrength(bearer));
		assertEquals(3, scn.GetStrength(ted));
		assertInDiscard(overrun);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// This skirmish ends; in the next, Squad of Haradrim's own, the bonus is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, squad));
		assertEquals(9, scn.GetStrength(squad));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtAPlainsSiteItMakesTheMenMinionStrengthPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var overrun = scn.GetShadowCard("overrun");
		var bearer = scn.GetShadowCard("bearer");
		scn.MoveCardsToHand(overrun);

		scn.StartGame();
		assertInHand(overrun);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bearer);

		assertEquals("Ettenmoors", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, bearer);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertEquals(10, scn.GetStrength(bearer));
		scn.ShadowPlayCard(overrun);

		// 10 + 4, in place of the +2 and not beside it.
		assertEquals(14, scn.GetStrength(bearer));
		assertInDiscard(overrun);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtABattlegroundSiteItMakesTheMenMinionStrengthPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var overrun = scn.GetShadowCard("overrun");
		var bearer = scn.GetShadowCard("bearer");
		scn.MoveCardsToHand(overrun);

		scn.StartGame();
		assertInHand(overrun);
		scn.MoveCardToAdventurePath(scn.GetShadowSite("fortress"));
		scn.FreepsPass();
		scn.MoveMinionsToTable(bearer);

		assertEquals("Fortress of Orthanc", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, bearer);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertEquals(10, scn.GetStrength(bearer));
		scn.ShadowPlayCard(overrun);

		assertEquals(14, scn.GetStrength(bearer));
		assertInDiscard(overrun);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoMenMinionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var overrun = scn.GetShadowCard("overrun");
		var brigand = scn.GetShadowCard("brigand");
		var lying = scn.GetShadowCard("lying");
		scn.MoveCardsToHand(overrun);
		scn.MoveCardsToSupportArea(lying);

		scn.StartGame();
		assertInHand(overrun);
		assertInZone(Zone.SUPPORT, lying);
		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, brigand);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(brigand));
		assertTrue(scn.ShadowPlayAvailable(overrun));
		scn.ShadowPlayCard(overrun);

		assertEquals(8, scn.GetStrength(brigand));
		assertInDiscard(overrun);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var overrun = scn.GetShadowCard("overrun");
		var bearer = scn.GetShadowCard("bearer");
		scn.MoveCardsToHand(overrun);

		scn.StartGame();
		assertInHand(overrun);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bearer);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.GetTwilight() >= 1);
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(overrun));
		assertInHand(overrun);
	}
}
