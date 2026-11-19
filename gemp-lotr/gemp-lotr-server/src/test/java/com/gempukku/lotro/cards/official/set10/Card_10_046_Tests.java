package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("quelled", "10_46");

					put("explorer", "4_250");   // Southron Explorer: a [raider] Southron Man, vitality 3; a regroup ability only
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Easterling Man, vitality 2 (fierce only at 2 burdens)
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, no text
					put("rearguard", "103_43"); // Bladetusk Rearguard: [raider] artifact that its maneuver ability makes a minion who is no Man
					put("harad", "4_238");      // Men of Harad: a [raider] support-area condition, a Shadow ability only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability only
					put("sword", "1_112");      // Ranger's Sword: a Free Peoples possession on Aragorn
					put("saga", "1_114");       // The Saga of Elendil: a [gondor] condition borne by a [gondor] companion
					put("mallorn", "1_54");     // Mallorn-trees: an [elven] support-area condition, a fellowship ability only
					put("luthien", "1_100");    // The Choice of Lúthien: a [gondor] support-area condition, a maneuver ability only
					put("rider", "4_265");      // Elite Rider: a [rohan] Man companion; +2 only skirmishing a wounded minion
					put("herdsman", "7_247");   // Rohirrim Herdsman: a [rohan] Man ally; a regroup ability only
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit, a Man only beside 3 [rohan] companions or Eowyn
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void QuelledStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Quelled
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Exert a [raider] Man to discard a Free Peoples condition. If you spot a [rohan] Man, discard an additional Free Peoples condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("quelled");

		assertEquals("Quelled", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsARaiderManToDiscardOneFreePeoplesCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quelled = scn.GetShadowCard("quelled");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		var brigand = scn.GetShadowCard("brigand");
		var rearguard = scn.GetShadowCard("rearguard");
		var harad = scn.GetShadowCard("harad");
		scn.MoveCardsToHand(quelled);
		scn.MoveCardsToSupportArea(rearguard, harad);

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var saga = scn.GetFreepsCard("saga");
		var mallorn = scn.GetFreepsCard("mallorn");
		var luthien = scn.GetFreepsCard("luthien");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword, saga);
		scn.MoveCardsToSupportArea(mallorn, luthien);
		assertInHand(quelled);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, mallorn, luthien, harad, rearguard);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, axeman, brigand);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(6);
		scn.FreepsPass();
		// The Rearguard is a [raider] minion who is no Man from here to the end of the turn.
		scn.ShadowUseCardAction(rearguard);
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertFalse(scn.IsRace(rearguard, Race.MAN));
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(quelled));
		scn.ShadowPlayCard(quelled);

		// The cost: [raider] Men of either keyword; not a Man of another culture, nor a [raider] minion who is no Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, axeman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, aragorn, rearguard));
		assertEquals(0, scn.GetWoundsOn(explorer));
		scn.ShadowChooseCard(explorer);
		assertEquals(1, scn.GetWoundsOn(explorer));

		// Free Peoples conditions, borne or in the support area, of two cultures; not a possession, nor a Shadow condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(saga, mallorn, luthien));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, harad));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(saga);

		// No [rohan] Man: one condition only.
		assertInDiscard(saga, quelled);
		assertInZone(Zone.SUPPORT, mallorn, luthien, harad);
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithARohanManCompanionASecondConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quelled = scn.GetShadowCard("quelled");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(quelled);

		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		var saga = scn.GetFreepsCard("saga");
		var mallorn = scn.GetFreepsCard("mallorn");
		var luthien = scn.GetFreepsCard("luthien");
		scn.MoveCompanionsToTable(aragorn, rider);
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveCardsToSupportArea(mallorn, luthien);
		assertInHand(quelled);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, mallorn, luthien);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(3);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(axeman));
		scn.ShadowPlayCard(quelled);
		// The Axeman is the one [raider] Man: he exerts unasked.
		assertEquals(1, scn.GetWoundsOn(axeman));

		scn.ShadowChooseCard(mallorn);
		assertInDiscard(mallorn);
		// The additional condition, chosen from those left.
		assertTrue(scn.ShadowHasCardChoiceAvailable(saga, luthien));
		scn.ShadowChooseCard(luthien);

		assertInDiscard(mallorn, luthien, quelled);
		assertAttachedTo(saga, aragorn);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ARohanManAllyIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quelled = scn.GetShadowCard("quelled");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(quelled);

		var aragorn = scn.GetFreepsCard("aragorn");
		var herdsman = scn.GetFreepsCard("herdsman");
		var saga = scn.GetFreepsCard("saga");
		var mallorn = scn.GetFreepsCard("mallorn");
		var luthien = scn.GetFreepsCard("luthien");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveCardsToSupportArea(mallorn, luthien);
		assertInHand(quelled);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, mallorn, luthien, herdsman);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(3);
		scn.FreepsPass();
		scn.ShadowPlayCard(quelled);
		assertEquals(1, scn.GetWoundsOn(axeman));

		scn.ShadowChooseCard(saga);
		assertTrue(scn.ShadowHasCardChoiceAvailable(mallorn, luthien));
		scn.ShadowChooseCard(mallorn);

		assertInDiscard(saga, mallorn, quelled);
		assertInZone(Zone.SUPPORT, luthien);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithNoRohanManOnlyOneConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quelled = scn.GetShadowCard("quelled");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(quelled);

		// A [rohan] card who is no Man, and a Man who is not [rohan].
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var saga = scn.GetFreepsCard("saga");
		var mallorn = scn.GetFreepsCard("mallorn");
		var luthien = scn.GetFreepsCard("luthien");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveCardsToSupportArea(mallorn, luthien);
		assertInHand(quelled);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, mallorn, luthien);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.ARCHERY);
		assertFalse(scn.IsRace(merry, Race.MAN));
		scn.SetTwilight(3);
		scn.FreepsPass();
		scn.ShadowPlayCard(quelled);
		assertEquals(1, scn.GetWoundsOn(axeman));

		scn.ShadowChooseCard(mallorn);

		assertInDiscard(mallorn, quelled);
		assertInZone(Zone.SUPPORT, luthien);
		assertAttachedTo(saga, aragorn);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithTheOnlyRaiderManExhaustedItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quelled = scn.GetShadowCard("quelled");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(quelled);

		var aragorn = scn.GetFreepsCard("aragorn");
		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(mallorn);
		assertInHand(quelled);
		assertInZone(Zone.SUPPORT, mallorn);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		// Vitality 2 with 1 wound: he cannot exert.  Aragorn, a Man of another culture, could.
		scn.AddWoundsToChar(axeman, 1);
		assertEquals(1, scn.GetWoundsOn(axeman));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(quelled));
	}

	@Test
	public void WithNoFreePeoplesConditionItIsPlayedAndDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quelled = scn.GetShadowCard("quelled");
		var axeman = scn.GetShadowCard("axeman");
		var harad = scn.GetShadowCard("harad");
		scn.MoveCardsToHand(quelled);
		scn.MoveCardsToSupportArea(harad);

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(aragorn, rider);
		scn.AttachCardsTo(aragorn, sword);
		assertInHand(quelled);
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, harad);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(quelled));
		scn.ShadowPlayCard(quelled);
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertInDiscard(quelled);
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, harad);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
