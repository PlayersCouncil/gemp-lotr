package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_171_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mount", "12_171");
					put("nertea", "1_234");     // Úlairë Nertëa, Messenger of Dol Guldur: a [wraith] Nazgûl, not fierce; his text is a play trigger, and he is cheated in
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl; nothing of his text below 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc and no Nazgûl; nothing below 2 wounds on the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Uruk Camp 4_335 (Battleground, Plains; exerts each Uruk-hai played, and none is) at site 2.
	// Site 1, Eastemnet Gullies 4_325, is neither battleground nor forest.
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mount", "12_171");
					put("nertea", "1_234");     // Úlairë Nertëa: a [wraith] Nazgûl who is not fierce, to bear the Mount
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: another Nazgûl, fierce only at 2 burdens, and there are none
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path as it is: Barrows of Edoras 4_337 (a sanctuary only) at site 3, White Rocks 4_346 (a battleground
	// and nothing else; its Shadow number counts mounted companions, and none is) at site 4.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mount", "12_171");
					put("nertea", "1_234");     // Úlairë Nertëa: a [wraith] Nazgûl who is not fierce, to bear the Mount
					put("nelya", "2_84");       // Úlairë Nelya: another Nazgûl, fierce only at 2 burdens, and there are none
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with two forests: Trollshaw Forest 1_334 at site 2 (Forest alone; draws when a possession is
	// played on a companion, and none is) and Rivendell Waterfall 1_342 at site 3 (Forest and Sanctuary; a move limit
	// bonus while a ranger is there, and none is).
	protected VirtualTableScenario GetForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mount", "12_171");
					put("nertea", "1_234");     // Úlairë Nertëa: a [wraith] Nazgûl who is not fierce, to bear the Mount
					put("nelya", "2_84");       // Úlairë Nelya: another Nazgûl, fierce only at 2 burdens, and there are none
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_334");
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path: site 3 is Rivendell Valley, Elrond's home, so that he may be assigned there.
	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// The card is not unique: two copies, so that the bearer can be told from another Nazgûl that rides one.
					put("mount", "12_171");
					put("mount2", "12_171");
					put("nertea", "1_234");     // Úlairë Nertëa: a [wraith] Nazgûl to bear the Mount
					put("nelya", "2_84");       // Úlairë Nelya: another Nazgûl; nothing below 2 burdens

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: an unbound [dwarven] Dwarf; a skirmish ability never used here
					put("boromir", "1_96");     // Boromir, Lord of Gondor: an unbound companion, to bear a Shadow condition of another culture
					put("foes", "11_196");      // Our Foes Are Weak: an [uruk-hai] Shadow condition on an unbound companion (cheated on); strength -1, resistance -3, no text
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally, home site 3; a fellowship ability

					// [wraith] conditions a character may come to bear, each cheated onto its bearer:
					put("dart", "8_69");        // Black Dart: transferred to an unbound companion; bearer cannot heal or bear mounts
					put("woods", "11_212");     // Lost in the Woods: transferred to a companion a Nazgûl skirmishes; +1 to [wraith] minions
					put("woods2", "11_212");    //   skirmishing bearer for each forest, and site 3 is none
					put("tip", "1_209");        // Blade Tip: transferred to the character who loses to a Nazgûl; wounds bearer at the start
					                            //   of each fellowship phase, and none comes
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShadowyMountStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Shadowy Mount
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Vitality: +1
		 * Game Text: Bearer must be a Nazgûl.<br>While bearer is at a battleground or forest site, he is <b>fierce</b>.<br><b>Assignment:</b> Make the Free Peoples player assign bearer to an unbound companion bearing a [wraith] condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mount");

		assertEquals("Shadowy Mount", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void IsPlayedOnANazgulOfAnyCultureAndNotOnAnotherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var mount = scn.GetShadowCard("mount");
		var nertea = scn.GetShadowCard("nertea");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(mount);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea, khamul, hound);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(mount));
		scn.ShadowPlayCard(mount);
		// A [wraith] Nazgûl and a [raider] one; not the [wraith] Orc, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nertea, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, frodo, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);
		assertAttachedTo(mount, khamul);
		assertNotAttachedTo(mount, nertea);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsFierceAtUrukCampAndNotBeforeIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var mount = scn.GetShadowCard("mount");
		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(nertea, nelya);
		scn.AttachCardsTo(nertea, mount);

		assertAttachedTo(mount, nertea);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(nertea, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(nelya, Keyword.FIERCE));

		// The fellowship moves to Uruk Camp, a battleground that is a plains as well.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(nertea, Keyword.FIERCE));
		// Another Nazgûl at the same site, who does not bear it, is untouched.
		assertFalse(scn.HasKeyword(nelya, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsFierceAtWhiteRocksAndNotAtTheSanctuaryBeforeIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var mount = scn.GetShadowCard("mount");
		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(nertea, nelya);
		scn.AttachCardsTo(nertea, mount);

		assertAttachedTo(mount, nertea);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(nertea, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(nelya, Keyword.FIERCE));

		// The fellowship moves to White Rocks, a battleground and nothing else.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(nertea, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(nelya, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsFierceAtEachForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetForestScenario();

		var mount = scn.GetShadowCard("mount");
		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(nertea, nelya);
		scn.AttachCardsTo(nertea, mount);

		assertAttachedTo(mount, nertea);
		// Trollshaw Forest, a forest and nothing else.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertTrue(scn.HasKeyword(nertea, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(nelya, Keyword.FIERCE));

		// Rivendell Waterfall, a forest that is a sanctuary as well.
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertTrue(scn.HasKeyword(nertea, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(nelya, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AssignmentAbilityHasTheFreePeoplesPlayerAssignBearerToAnUnboundCompanionBearingAWraithConditionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var elrond = scn.GetFreepsCard("elrond");

		var mount = scn.GetShadowCard("mount");
		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		var dart = scn.GetShadowCard("dart");
		var woods = scn.GetShadowCard("woods");
		var woods2 = scn.GetShadowCard("woods2");
		var tip = scn.GetShadowCard("tip");
		var foes = scn.GetShadowCard("foes");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, sam);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(aragorn, dart);
		scn.AttachCardsTo(gimli, woods);
		scn.AttachCardsTo(sam, woods2);
		scn.AttachCardsTo(elrond, tip);
		scn.AttachCardsTo(boromir, foes);
		scn.MoveMinionsToTable(nertea, nelya);
		scn.AttachCardsTo(nertea, mount);

		assertAttachedTo(dart, aragorn);
		assertAttachedTo(woods, gimli);
		assertAttachedTo(woods2, sam);
		assertAttachedTo(tip, elrond);
		assertAttachedTo(foes, boromir);
		assertAttachedTo(mount, nertea);

		// The maneuver phase, next door to the assignment phase: not offered.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mount));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(mount));

		scn.ShadowUseCardAction(mount);

		// The Free Peoples player chooses between Aragorn and Gimli, unbound companions of two cultures bearing two
		// different [wraith] conditions.  Not Boromir, an unbound companion who bears a Shadow condition of another culture, an [uruk-hai] one; not Sam, who bears one
		// and is Ring-bound; not Elrond, who bears one at his home site, where he may be assigned, and is no
		// companion; not the Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, sam, elrond, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// The bearer is assigned to him, and the other Nazgûl to nobody.
		assertTrue(scn.IsCharAssignedAgainst(gimli, nertea));
		assertFalse(scn.IsCharAssigned(nelya));
		assertFalse(scn.IsCharAssigned(aragorn));

		// The bearer assigned, the ability is not offered again, though Aragorn is still there to assign him to.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mount));
	}

	@Test
	public void WithTheOneCompanionItCouldNameAssignedTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var mount = scn.GetShadowCard("mount");
		var mount2 = scn.GetShadowCard("mount2");
		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		var dart = scn.GetShadowCard("dart");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, dart);
		scn.MoveMinionsToTable(nertea, nelya);
		scn.AttachCardsTo(nertea, mount);
		scn.AttachCardsTo(nelya, mount2);

		assertAttachedTo(dart, aragorn);
		assertAttachedTo(mount, nertea);
		assertAttachedTo(mount2, nelya);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(mount));
		assertTrue(scn.ShadowActionAvailable(mount2));

		// Aragorn is the one unbound companion bearing a [wraith] condition: he is assigned to Nelya, the bearer of
		// the copy used, with nothing asked.
		scn.ShadowUseCardAction(mount2);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, nelya));

		// Now nobody is left to assign Nertëa to: his copy does not offer its ability, though he is unassigned.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(nertea));
		assertFalse(scn.IsCharAssigned(boromir));
		assertFalse(scn.ShadowActionAvailable(mount));
	}
}
