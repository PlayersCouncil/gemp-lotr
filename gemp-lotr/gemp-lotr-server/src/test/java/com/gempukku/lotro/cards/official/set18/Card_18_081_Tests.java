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

public class Card_18_081_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warg", "18_81");
					put("denizen", "11_115");    // Denizen of Khazad-dum: [orc] Orc, strength 9, vitality 2; +2 only at an underground site
					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc, strength 5; its play trigger wants an underground site
					put("blackpit", "11_117");   // Denizen of the Black Pit: [orc] Orc, strength 10; +2 only at an underground site
					put("halftroll", "14_12");   // Half-troll of Far Harad: an [orc] minion who is no Orc, strength 8; a start-of-assignment trigger
					put("runner", "1_178");      // Goblin Runner: a [moria] Orc, strength 5; its trigger is on being played
					// Gothmog, Morgul Leader: [orc] Orc, strength 15; archery totals +3 and a regroup ability (read in the Shadow phase only)
					put("leader", "18_80");
					// Gothmog, Lieutenant of Morgul: [sauron] Orc, strength 13, vitality 3; Besieger; his play trigger is not
					// reached by a cheat, and he is fierce only while his player controls a site
					put("lieutenant", "10_88");
					// Gothmog, Morgul Commander: [wraith] Orc, strength 13, vitality 3; each [wraith] minion an archer only while
					// the Shadow player has initiative
					put("commander", "8_72");
					put("pursuer", "4_190");     // Uruk Pursuer: [isengard] Uruk-hai, strength 5, vitality 3, fierce; his texts are about unbound Hobbits

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("gimli", "1_13");        // Gimli, Son of Gloin: strength 6, vitality 3, damage +1; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GothmogsWargStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Gothmog's Warg, Leader's Mount
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +1
		 * Vitality: +1
		 * Game Text: Bearer must be an [orc] Orc with strength 9 or less (or bearer must be Gothmog).<br>Bearer is <b>fierce</b>.<br>While bearer is Gothmog, he is <b>damage +1</b> in fierce skirmishes.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warg");

		assertEquals("Gothmog's Warg", card.getBlueprint().getTitle());
		assertEquals("Leader's Mount", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnAnOrcOrcOfStrength9OrLessOrOnGothmogAndMakesItFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var denizen = scn.GetShadowCard("denizen");
		var goblin = scn.GetShadowCard("goblin");
		var blackpit = scn.GetShadowCard("blackpit");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");
		var leader = scn.GetShadowCard("leader");
		scn.MoveCardsToHand(warg);
		assertInHand(warg);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, goblin, blackpit, halftroll, runner, leader);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(denizen, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(goblin, Keyword.FIERCE));
		assertTrue(scn.ShadowPlayAvailable(warg));

		scn.ShadowPlayCard(warg);
		// [orc] Orcs of strength 9 (the bound) and 5, and Gothmog of 15 by the second half.  Not an [orc] Orc of 10,
		// an [orc] Half-troll, or a [moria] Orc of 5.  Site 2 is not underground: every strength is the printed one.
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, goblin, leader));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(blackpit, halftroll, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(denizen);

		assertAttachedTo(warg, denizen);
		assertTrue(scn.HasKeyword(denizen, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(goblin, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnGothmogItIsDamagePlus1InHisFierceSkirmishAndInNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var lieutenant = scn.GetShadowCard("lieutenant");
		var pursuer = scn.GetShadowCard("pursuer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(warg);
		assertInHand(warg);

		scn.StartGame();
		scn.MoveMinionsToTable(lieutenant, pursuer);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// A [sauron] Gothmog of 13 is no [orc] Orc of 9 or less: only the second half of the text takes him, and he is
		// the one bearer on the table, chosen unasked.
		assertFalse(scn.HasKeyword(lieutenant, Keyword.FIERCE));
		assertTrue(scn.ShadowPlayAvailable(warg));
		scn.ShadowPlayCard(warg);
		assertAttachedTo(warg, lieutenant);
		assertTrue(scn.HasKeyword(lieutenant, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lieutenant}, new PhysicalCardImpl[]{gimli, pursuer});

		// The normal skirmish is no fierce skirmish.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetKeywordCount(lieutenant, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		// 13 + 1 against 8: one wound.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		// Gimli wins, damage +1: two wounds, and the Pursuer lives to skirmish again.
		assertEquals(2, scn.GetWoundsOn(pursuer));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lieutenant}, new PhysicalCardImpl[]{gimli, pursuer});

		// Assigned, but waiting while another fierce skirmish is fought: not in a fierce skirmish yet.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetKeywordCount(lieutenant, Keyword.DAMAGE));
		scn.PassFierceSkirmishActions();

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(1, scn.GetKeywordCount(lieutenant, Keyword.DAMAGE));
		scn.PassFierceSkirmishActions();
		// 1 from the normal round, then 1 + 1 for the damage bonus.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BorneByAnotherOrcNoDamageBonusGoesToItOrToAGothmogInHisOwnFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var denizen = scn.GetShadowCard("denizen");
		var lieutenant = scn.GetShadowCard("lieutenant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		// Both markers past site 1, so that it can be controlled.
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(denizen, lieutenant);
		scn.AttachCardsTo(denizen, warg);
		assertAttachedTo(warg, denizen);
		assertFalse(scn.HasKeyword(lieutenant, Keyword.FIERCE));
		// A site controlled makes this Gothmog, who does not bear the Warg, fierce by his own text: he has a fierce
		// skirmish too.
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.HasKeyword(lieutenant, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lieutenant}, new PhysicalCardImpl[]{gimli, denizen});
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// 9 + 1 against Gimli's 6, and 13 against Aragorn's 8: one wound each.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lieutenant}, new PhysicalCardImpl[]{gimli, denizen});

		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetKeywordCount(denizen, Keyword.DAMAGE));
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetKeywordCount(lieutenant, Keyword.DAMAGE));
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnAWraithGothmogTooItIsDamagePlus1InHisFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var commander = scn.GetShadowCard("commander");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Four cards in the Free Peoples hand give him initiative, so that this Gothmog is no archer.
		var hand1 = scn.GetFreepsCard("goblin");
		var hand2 = scn.GetFreepsCard("blackpit");
		var hand3 = scn.GetFreepsCard("halftroll");
		var hand4 = scn.GetFreepsCard("runner");
		scn.MoveCardsToHand(hand1, hand2, hand3, hand4);
		assertInHand(hand1, hand2, hand3, hand4);

		scn.StartGame();
		scn.MoveMinionsToTable(commander);
		scn.AttachCardsTo(commander, warg);
		assertAttachedTo(warg, commander);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, commander);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetKeywordCount(commander, Keyword.DAMAGE));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, commander);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(1, scn.GetKeywordCount(commander, Keyword.DAMAGE));
		scn.PassFierceSkirmishActions();
		// 1 from the normal round, then 1 + 1 for the damage bonus.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
