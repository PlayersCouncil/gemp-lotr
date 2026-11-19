package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_108_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("olog", "11_108");
					put("olog2", "11_108");

					// Half-troll of Far Harad: an [orc] minion who is no Orc, cost 3, site 4.  Its trigger at the start
					// of the assignment phase needs more companions than minions.
					put("halftroll", "14_12");
					// Denizen of Khazad-dum: an [orc] Orc.  Strength +2 only at an underground site, never reached.
					put("denizen", "11_115");
					// Goblin Patrol Troop: a [moria] Orc, no text.
					put("troop", "1_177");
					// The Beckoning Shadow: an [orc] condition (support area).  Its corruption needs 5 burdens and an
					// empty Free Peoples draw deck.
					put("beckoning", "12_83");
					// Ithil Stone: a [sauron] artifact in the support area.  An optional draw at the start of the
					// Shadow phase, which the skip declines, and a Shadow ability, never used.
					put("ithil", "9_47");

					// Aragorn, Ranger of the North: a [gondor] Man.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Ring of Barahir: a [gondor] artifact borne by Aragorn.  An optional heal at the start of the turn,
					// so it is attached after the game starts.
					put("barahir", "6_55");
					// Sam, Son of Hamfast: a [shire] Hobbit, Ring-bound.  His abilities are never used.
					put("sam", "1_311");
					// Phial of Galadriel, Star-glass: an [elven] artifact borne by a Hobbit.  A skirmish ability, never used.
					put("phial", "10_13");
					// Gimli, Son of Gloin: a [dwarven] Dwarf.  A skirmish ability, never used.
					put("gimli", "1_13");
					// Dwarven Armor: a possession, no artifact, borne by Gimli.
					put("armor", "1_8");
					// Thrarin, Dwarven Smith: a Dwarf ally.  "Maneuver: Exert Thrarin to allow him to participate in
					// archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
					// Ring of Retribution: a [dwarven] artifact borne by a Dwarf.  Its response answers its bearer
					// winning a skirmish, never reached.
					put("retribution", "9_9");
					// The Shards of Narsil: a [gondor] artifact in the support area.  Fellowship abilities, never used.
					put("shards", "3_44");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeastlyOloghaiStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Beastly Olog-hai
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. <b>Fierce</b>. To play, spot an [orc] minion.
		 * 		<b>Assignment:</b> Exert this minion to assign it to a companion bearing an artifact. The Free Peoples player may discard an artifact from play to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("olog");

		assertEquals("Beastly Olog-hai", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.TROLL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilAnOrcMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var olog = scn.GetShadowCard("olog");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");

		scn.StartGame();
		scn.SetShadowHand(olog, halftroll);
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(beckoning);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.SUPPORT, beckoning);
		scn.FreepsPass();

		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// A [moria] Orc minion and an [orc] condition: no [orc] minion to spot.
		assertFalse(scn.ShadowPlayAvailable(olog));

		// The Half-troll, an [orc] minion who is no Orc, played from hand.
		scn.ShadowPlayCard(halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		assertTrue(scn.ShadowPlayAvailable(olog));

		scn.ShadowPlayCard(olog);
		assertInZone(Zone.SHADOW_CHARACTERS, olog);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedWithAnOrcOrcToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var olog = scn.GetShadowCard("olog");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.SetShadowHand(olog);
		scn.MoveMinionsToTable(denizen);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		scn.FreepsPass();

		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(olog));

		scn.ShadowPlayCard(olog);
		assertInZone(Zone.SHADOW_CHARACTERS, olog);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ExertsToAssignItselfToACompanionBearingAnArtifactWhenTheFreePeoplesPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var barahir = scn.GetFreepsCard("barahir");
		var sam = scn.GetFreepsCard("sam");
		var phial = scn.GetFreepsCard("phial");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var thrarin = scn.GetFreepsCard("thrarin");
		var retribution = scn.GetFreepsCard("retribution");
		scn.MoveCompanionsToTable(aragorn, sam, gimli);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(sam, phial);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(thrarin, retribution);

		var olog = scn.GetShadowCard("olog");
		var olog2 = scn.GetShadowCard("olog2");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, barahir);
		scn.MoveMinionsToTable(olog, olog2);
		assertAttachedTo(barahir, aragorn);
		assertAttachedTo(phial, sam);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(retribution, thrarin);

		// Thrarin made able to skirmish, so that only "companion" keeps him out.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertEquals(0, scn.GetWoundsOn(olog));
		assertEquals(0, scn.GetWoundsOn(olog2));
		assertTrue(scn.ShadowActionAvailable(olog));
		scn.ShadowUseCardAction(olog);
		assertEquals(1, scn.GetWoundsOn(olog));

		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		// Frodo bears only The One Ring, which is no artifact; Gimli a possession; Thrarin is an ally.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, gimli, thrarin));
		scn.ShadowChooseCard(aragorn);

		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard"));
		scn.FreepsChooseNo();

		assertTrue(scn.IsCharAssignedAgainst(aragorn, olog));
		assertAttachedTo(barahir, aragorn);
		// The other copy is neither exerted nor assigned.
		assertEquals(0, scn.GetWoundsOn(olog2));
		assertFalse(scn.IsCharAssigned(olog2));
		assertFalse(scn.IsCharAssigned(sam));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayDiscardAnyArtifactInPlayToPreventTheAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var barahir = scn.GetFreepsCard("barahir");
		var sam = scn.GetFreepsCard("sam");
		var phial = scn.GetFreepsCard("phial");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var thrarin = scn.GetFreepsCard("thrarin");
		var retribution = scn.GetFreepsCard("retribution");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCompanionsToTable(aragorn, sam, gimli);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(shards);
		scn.AttachCardsTo(sam, phial);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(thrarin, retribution);

		var olog = scn.GetShadowCard("olog");
		var ithil = scn.GetShadowCard("ithil");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, barahir);
		scn.MoveMinionsToTable(olog);
		scn.MoveCardsToSupportArea(ithil);
		assertAttachedTo(barahir, aragorn);
		assertAttachedTo(phial, sam);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(retribution, thrarin);
		assertInZone(Zone.SUPPORT, shards, ithil);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		scn.ShadowUseCardAction(olog);
		assertEquals(1, scn.GetWoundsOn(olog));
		scn.ShadowChooseCard(aragorn);

		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard"));
		scn.FreepsChooseYes();

		// An artifact from play: borne by the companion chosen, by another, by an ally, in either support area.
		assertTrue(scn.FreepsHasCardChoiceAvailable(barahir, phial, retribution, shards, ithil));
		// A possession, and The One Ring, which is no artifact.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor, scn.GetRing()));
		scn.FreepsChooseCard(phial);

		assertInDiscard(phial);
		assertAttachedTo(barahir, aragorn);
		assertInZone(Zone.SUPPORT, shards, ithil);
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(olog));
		// The exertion stays paid.
		assertEquals(1, scn.GetWoundsOn(olog));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhileExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var barahir = scn.GetFreepsCard("barahir");
		scn.MoveCompanionsToTable(aragorn);

		var olog = scn.GetShadowCard("olog");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, barahir);
		scn.MoveMinionsToTable(olog);
		scn.AddWoundsToChar(olog, 2);
		assertAttachedTo(barahir, aragorn);
		assertEquals(2, scn.GetWoundsOn(olog));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(olog));
	}

	@Test
	public void WithNoCompanionBearingAnArtifactItExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(gimli, armor);

		var olog = scn.GetShadowCard("olog");

		scn.StartGame();
		scn.MoveMinionsToTable(olog);
		assertAttachedTo(armor, gimli);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertEquals(0, scn.GetWoundsOn(olog));
		assertTrue(scn.ShadowActionAvailable(olog));
		scn.ShadowUseCardAction(olog);

		assertEquals(1, scn.GetWoundsOn(olog));
		assertFalse(scn.IsCharAssigned(olog));
		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard"));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
