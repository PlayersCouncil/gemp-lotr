package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_06_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("liar", "6_47");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5, vitality 4; a regroup ability used only on purpose
					put("wraithgollum", "103_66"); // Gollum, Half a Wraith Himself: the [wraith] Gollum, strength 5; his abilities are used only on purpose
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum, strength 8, fierce
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, strength 3, vitality 4; his abilities are used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an ally; a Fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void YoureaLiarandaThiefStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: You're a Liar and a Thief
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If Gollum wins a skirmish, wound a companion (except the Ring- bearer).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("liar");

		assertEquals("You're a Liar and a Thief", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenGollumWinsTheShadowPlayerWoundsACompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var liar = scn.GetShadowCard("liar");
		var gollum = scn.GetShadowCard("gollum");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(liar);

		scn.StartGame();
		assertInHand(liar);
		// Vitality 4 with 3 wounds: Aragorn is exhausted, and a wound kills him.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, gollum);
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPass();

		// Nobody has won anything yet.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(liar));

		// 5 against 3: Gollum wins.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowPlayAvailable(liar));
		scn.ShadowPlayCard(liar);

		// Two companions who are not the Ring-bearer, the exhausted one among them; not the Ring-bearer, nor an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, celeborn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// A wound, not an exertion: the exhausted Aragorn is killed.
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertInZone(Zone.DISCARD, liar);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheWraithGollumWinsItIsPlayableToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var liar = scn.GetShadowCard("liar");
		var gollum = scn.GetShadowCard("wraithgollum");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(liar);

		scn.StartGame();
		assertInHand(liar);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, gollum);
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		// 5 against 3: the [wraith] Gollum wins.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowPlayAvailable(liar));
		scn.ShadowPlayCard(liar);

		// Sam is the one companion who is not the Ring-bearer, and is wounded unasked.
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DISCARD, liar);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayableWhenAGollumMinionWhoIsNoGollumWinsNorWhenGollumLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var liar = scn.GetShadowCard("liar");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(liar);

		scn.StartGame();
		assertInHand(liar);
		scn.MoveMinionsToTable(gollum, shelob);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, shelob}, new PhysicalCardImpl[]{aragorn, gollum});

		// 8 against 6, not doubled: Shelob, a [gollum] minion who is no Gollum, wins.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		// The Shadow player is asked nothing: where the event can be played, he is asked here.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// 5 against 8, not doubled: Gollum loses, and lives through his wound.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInHand(liar);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// Shelob is fierce: the fierce assignments come next.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithOnlyTheRingBearerToWoundItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var liar = scn.GetShadowCard("liar");
		var gollum = scn.GetShadowCard("gollum");
		var celeborn = scn.GetFreepsCard("celeborn");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(liar);

		scn.StartGame();
		assertInHand(liar);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, gollum);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();

		// 5 against Frodo's 4 (3 and the Ring's +1): Gollum wins. The Ring's offer for the wound is declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));

		// No companion but the Ring-bearer: the event is still played, and wounds nobody.
		assertTrue(scn.ShadowPlayAvailable(liar));
		scn.ShadowPlayCard(liar);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertInZone(Zone.DISCARD, liar);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
